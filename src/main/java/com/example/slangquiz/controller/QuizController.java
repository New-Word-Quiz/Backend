package com.example.slangquiz.controller;

import com.example.slangquiz.domain.Hint;
import com.example.slangquiz.service.QuizService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/*
 * 컨트롤러 규칙
 * - 세션 인증 : Cookie: session_id=...
 * - 경로 단순화 : /start, /next, /hint, /answer, /explanation, /result
 * - GET 응답 헤더 : Cache-Control: no-store, private / Vary: Cookie
 * - Json 키 : application.yml의 SNAKE_CASE 로 자동 스네이크케이스
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;  // 서비스 주입

    // GET 응답 캐시 금지 + 쿠키 의존성 명시
    private HttpHeaders noStoreHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setCacheControl(CacheControl.noStore().cachePrivate());
        h.add(HttpHeaders.VARY, "Cookie");
        return h;
    }

    // 세션 쿠키: Path=/, HttpOnly, SameSite=Lax, Max-Age=1일, secure=true
    private ResponseCookie buildSessionCookie(long sessionId, boolean secure) {
        return ResponseCookie.from("session_id", String.valueOf(sessionId))
                .httpOnly(true)
                .secure(secure) // 로컬 : false, 배포 : true
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
    }

    // ===== DTO =====
    public record StartRequest(String nickname) {}
    public record StartResponse(int status, String message, long session_id, int quiz_count) {}

    // /next
    public record NextQuestionResponse(
            int status, String message,
            int quiz_order, long quiz_id, String quiz_text, String image_url,
            List<OptionItem> options,
            int answered_count, int remaining_count, boolean is_last   // 진행도
    ) { public record OptionItem(long option_id, int answer_number, String option_text) {} }

    // /hint
    public record HintResponse(
            int status, String message,
            int quiz_order, long quiz_id, String hint_text, String example_text
    ) {}

    // /answer
    public record AnswerRequest(long option_id) {}
    public record AnswerResponse(
            int status, String message, boolean is_correct,
            long selected_option_id, int quiz_order, long quiz_id,
            long option_id, String option_text, String option_meaning, String example_text
    ) {}

    // /explanation
    public record ExplanationResponse(
            int status, String message, int quiz_order, long quiz_id,
            List<ExplanationItem> explanations
    ) { public record ExplanationItem(long option_id, String option_text, String option_meaning) {} }

    // /result
    public record ResultResponse(
            int status, String message, String nickname,
            int score, int answered_count, int total_count
    ) {}

    // ===== 엔드포인트 =====

    // 게임 시작 : 닉네임 - 유저 확보 - 세션 생성 - 문제 20개 고정
    @PostMapping("/start")
    public ResponseEntity<StartResponse> start(@RequestBody StartRequest req) {
        // (검증) 닉네임 필수 + 1~5자
        if (req == null || req.nickname() == null || req.nickname().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new StartResponse(400, "닉네임은 필수입니다.", 0L, 0));
        }
        String nickname = req.nickname().trim();
        if (nickname.length() > 5) {
            return ResponseEntity.badRequest().body(new StartResponse(400, "닉네임은 5글자 이하여야 합니다.", 0L, 0));
        }

        long sessionId = quizService.startGame(nickname);
        ResponseCookie cookie = buildSessionCookie(sessionId, false);   // 로컬이라 false

        StartResponse body = new StartResponse(201, "게임 시작", sessionId, 20);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    // 다음 문제 조회 (미제출 중 최솟값만 제공)
    @GetMapping("/next")
    public ResponseEntity<?> next(@CookieValue(name = "session_id", required = true) long sessionId) {
        var maybe = quizService.getNextQuestion(sessionId);
        if (maybe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .headers(noStoreHeaders())
                    .body(Map.of("status", 409, "error", "Conflict", "message", "모든 문제를 완료했습니다."));
        }
        var nq = maybe.get();
        var optionItems = nq.options().stream()
                .map(o -> new NextQuestionResponse.OptionItem(o.optionId(), o.answerNumber(), o.optionText()))
                .toList();
        var body = new NextQuestionResponse(
                200, "다음 문제 제공 성공",
                nq.order(), nq.quizId(), nq.quizText(), nq.imageUrl(),
                optionItems, nq.answeredCount(), nq.remainingCount(), nq.isLast()
        );
        return ResponseEntity.ok()
                .headers(noStoreHeaders())
                .body(body);
    }

    // 힌트 조회 (현재 제출 가능 문제의 힌트 반환)
    @GetMapping("/hint")
    public ResponseEntity<?> hint(@CookieValue(name = "session_id", required = true) long sessionId) {
        var maybeNext = quizService.getNextQuestion(sessionId);
        if (maybeNext.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .headers(noStoreHeaders())
                    .body(Map.of("status", 409, "error", "Conflict", "message", "모든 문제를 완료했습니다."));
        }
        var nq = maybeNext.get();

        var maybeHint = quizService.getHintForQuiz(nq.quizId());
        if (maybeHint.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .headers(noStoreHeaders())
                    .body(Map.of("status", 404, "message", "해당 문제의 힌트가 없습니다."));
        }
        Hint h = maybeHint.get();
        var body = new HintResponse(
                200, "힌트 제공 성공",
                nq.order(), nq.quizId(), h.getHintText(), h.getExampleText()
        );
        return ResponseEntity.ok()
                .headers(noStoreHeaders())
                .body(body);
    }

    // 정답 제출 (현재 제출 가능한 문제만; 포인터리스)
    @PostMapping("/answer")
    public ResponseEntity<AnswerResponse> answer(
            @CookieValue(name = "session_id", required = true) long sessionId,
            @RequestBody AnswerRequest req
    ) {
        if (req == null) {
            return ResponseEntity.badRequest().body(new AnswerResponse(400, "요청 바디가 필요합니다.",
                    false, 0,0,0,0,null,null,null));
        }
        var r = quizService.submitAnswer(sessionId, req.option_id());
        var body = new AnswerResponse(
                201, r.isCorrect() ? "정답입니다." : "오답입니다.", r.isCorrect(),
                req.option_id(), r.quizOrder(), r.quizId(),
                r.correctOptionId(), r.correctText(), r.correctMeaning(), r.correctExample()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // 오답 해설 보기 (최근 제출 문제 기준)
    @GetMapping("/explanation")
    public ResponseEntity<ExplanationResponse> explanation(@CookieValue(name = "session_id", required = true) long sessionId) {
        var dto = quizService.getExplanation(sessionId);                            // 서비스 신규 호출
        var items = dto.items().stream()
                .map(i -> new ExplanationResponse.ExplanationItem(i.optionId(), i.optionText(), i.optionMeaning()))
                .toList();
        var body = new ExplanationResponse(200, "오답 보기 해설 제공 성공", dto.quizOrder(), dto.quizId(), items);
        return ResponseEntity.ok().headers(noStoreHeaders()).body(body);
    }

    // 결과 화면(점수 계산 및 세션 저장)
    @GetMapping("/result")
    public ResponseEntity<ResultResponse> result(@CookieValue(name = "session_id", required = true) long sessionId) {
        var r = quizService.getResult(sessionId);
        var body = new ResultResponse(200, "결과 조회 성공",
                r.nickname(), r.score(), r.answeredCount(), r.totalCount());
        return ResponseEntity.ok().headers(noStoreHeaders()).body(body);
    }
}
