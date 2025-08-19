package com.example.slangquiz.service;

import com.example.slangquiz.domain.*;
import com.example.slangquiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * startGame(nickname) : 유저 생성/조회 -> 세션 생성 -> 문제 20개 배정
 * submitAnswer        : 정답, 오답 판정 + 풀이 기록 저장
 * getHintForQuiz      : 힌트 조회 (퀴즈ID 기준)
 * getNextQuestion     : 다음 문제 + 보기 조회 (미제출 최솟값)
 * getExplanation      : 최근 제출 문제의 오답 3개 해설
 * getResult           : 점수 계산 후 세션에 최초 1회 저장, 결과 조회
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    // ===== Repository 주입 =====
    private final UserRepository userRepo;
    private final QuizRepository quizRepo;
    private final HintRepository hintRepo;
    private final QuizOptionRepository optionRepo;
    private final QuizSessionRepository sessionRepo;
    private final SessionQuizRepository sessionQuizRepo;
    private final SessionAnswerRepository answerRepo;

    /**
     * 트랜잭션(쓰기)
     * - 메서드 시작 시 트랜잭션 시작, 정상 종료 시 커밋
     * - 도중에 RunTimeException 발생 시 롤백
     */
    @Transactional
    public Long startGame(String nickname) {
        User user = userRepo.findFirstByNicknameOrderByUserIdDesc(nickname)
                .orElseGet(() -> userRepo.save(new User(nickname)));

        // 세션 생성
        QuizSession session = sessionRepo.save(new QuizSession(user));

        // DB에서 바로 20개 랜덤
        List<Quiz> pick20 = quizRepo.pickRandom(20);
        if (pick20.size() < 20) {
            throw new IllegalStateException("퀴즈가 20개 미만입니다. 현재 개수 : " + pick20.size());
        }

        // 배정된 문제를 세션에 순서대로 저장 (1~20)
        int order = 1;
        for (Quiz q : pick20) {
            sessionQuizRepo.save(new SessionQuiz(session, q, order++));
        }
        return session.getSessionId();
    }

    // ===== DTO (엔티티 직접 노출 방지; open-in-view=false 대응) =====
    public record NextQuestionDTO(
            long quizId, String quizText, String imageUrl,
            List<OptionItem> options,
            int order, int answeredCount, int remainingCount, boolean isLast
    ) { public record OptionItem(long optionId, int answerNumber, String optionText) {} }

    public record AnswerResult(
            boolean isCorrect, int quizOrder, long quizId,
            long correctOptionId, String correctText, String correctMeaning, String correctExample
    ) {}

    public record ExplanationDTO(int quizOrder, long quizId, List<Item> items) {
        public record Item(long optionId, String optionText, String optionMeaning) {}
    }

    public record ResultDTO(String nickname, int score, int answeredCount, int totalCount) {}

    /**
     * 트랜잭션(읽기)
     * - 다음 문제(미제출 중 최솟값) + 보기 목록
     */
    @Transactional(readOnly = true)
    public java.util.Optional<NextQuestionDTO> getNextQuestion(Long sessionId) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("세션 없음 : " + sessionId));

        var nextList = sessionQuizRepo.findNextUnanswered(session, PageRequest.of(0, 1));
        if (nextList.isEmpty()) return java.util.Optional.empty(); // 완료 시 컨트롤러에서 409로 매핑

        SessionQuiz sq = nextList.get(0);
        long quizId = sq.getQuiz().getQuizId();

        List<QuizOption> options = optionRepo.findByQuiz_QuizIdOrderByAnswerNumberAsc(quizId);
        var items = options.stream()
                .map(o -> new NextQuestionDTO.OptionItem(o.getOptionId(), o.getAnswerNumber(), o.getOptionText()))
                .toList();

        int answered = (int) answerRepo.countBySessionQuiz_Session(session);  // 진행도 제공
        int remaining = 20 - answered;
        boolean isLast = remaining == 1;

        return java.util.Optional.of(new NextQuestionDTO(
                quizId, sq.getQuiz().getQuizText(), sq.getQuiz().getImageUrl(),
                items, sq.getQuizOrder(), answered, remaining, isLast
        ));
    }

    /**
     * 트랜잭션(읽기)
     * - 힌트 조회
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Hint> getHintForQuiz(long quizId) {
        return hintRepo.findByQuiz_QuizId(quizId);
    }

    /**
     * 트랜잭션(쓰기)
     * - 정답 제출 + 기록 저장
     * - 포인터리스 규칙: 현재 제출 가능한 문제(미제출 최솟값)에 대해 optionId만 제출
     */
    @Transactional
    public AnswerResult submitAnswer(Long sessionId, Long optionId) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("세션 없음 : " + sessionId));

        var nextList = sessionQuizRepo.findNextUnanswered(session, PageRequest.of(0, 1));
        if (nextList.isEmpty()) throw new IllegalStateException("모든 문제를 완료했습니다.");

        SessionQuiz sq = nextList.get(0);
        long quizId = sq.getQuiz().getQuizId();

        // 현재 문제의 보기인지 검증
        QuizOption selected = optionRepo
                .findByQuiz_QuizIdAndOptionId(quizId, optionId)
                .orElseThrow(() -> new IllegalArgumentException("현재 문제의 보기만 제출할 수 있습니다."));

        // 중복 제출 방지
        if (answerRepo.existsBySessionQuiz(sq)) {
            throw new IllegalStateException("이미 정답을 제출한 문제입니다.");
        }

        boolean isCorrect = Boolean.TRUE.equals(selected.getIsAnswer());

        // SessionAnswer은 (SessionQuiz, QuizOption, Boolean)
        answerRepo.save(new SessionAnswer(sq, selected, isCorrect));

        // 정답 보기 조회 (정답 1개 보장)
        var correct = optionRepo.findByQuiz_QuizIdAndIsAnswerTrue(quizId)
                .orElseThrow(() -> new IllegalStateException("정답 보기가 정의되지 않았습니다."));

        return new AnswerResult(
                isCorrect, sq.getQuizOrder(), quizId,
                correct.getOptionId(), correct.getOptionText(), correct.getOptionMeaning(), correct.getExampleText()
        );
    }

    /**
     * 트랜잭션(읽기)
     * - 최근 제출 기준의 오답 해설 3개 반환
     */
    @Transactional(readOnly = true)
    public ExplanationDTO getExplanation(Long sessionId) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("세션 없음 : " + sessionId));

        var last = answerRepo.findTopBySessionQuiz_SessionOrderByCreatedAtDesc(session)
                .orElseThrow(() -> new NoSuchElementException("최근 제출한 답안이 없습니다."));

        int order = last.getSessionQuiz().getQuizOrder();
        long quizId = last.getSessionQuiz().getQuiz().getQuizId();

        var wrongs = optionRepo.findByQuiz_QuizIdAndIsAnswerFalseOrderByAnswerNumberAsc(quizId);
        var items = wrongs.stream()
                .map(o -> new ExplanationDTO.Item(o.getOptionId(), o.getOptionText(), o.getOptionMeaning()))
                .toList();

        return new ExplanationDTO(order, quizId, items);
    }

    /**
     * 트랜잭션(쓰기)
     * - 결과 계산 후 세션에 점수 저장(최초 1회만)
     */
    @Transactional
    public ResultDTO getResult(Long sessionId) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("세션 없음 : " + sessionId));

        int answered = (int) answerRepo.countBySessionQuiz_Session(session);
        if (answered < 20) throw new IllegalStateException("아직 모든 문제를 완료하지 않았습니다.");

        int correct = (int) answerRepo.countBySessionQuiz_SessionAndIsCorrectTrue(session);
        int score = correct * 5;  // 1문제 = 5점

        if (session.getScore() == null) {       // 최초 1회 저장, 재호출 시 덮어쓰기 방지
            session.setScore(score);
        }
        return new ResultDTO(session.getUser().getNickname(), score, answered, 20);
    }
}
