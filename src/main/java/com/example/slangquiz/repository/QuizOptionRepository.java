package com.example.slangquiz.repository;

import com.example.slangquiz.domain.Quiz;   // Quiz 엔티티
import com.example.slangquiz.domain.QuizOption;     // QuizOption 엔티티
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;      // 여러 개의 엔티티를 순서 있는 컬렉션 형태로 담는 자료형
import java.util.Optional;  // 단건 조회

// QuizOption 엔티티용 Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

    // 특정 퀴즈의 보기를 보기 순서(answerNumber) 기준으로 정렬하여  조회
    List<QuizOption> findByQuizOrderByAnswerNumberAsc(Quiz quiz);

    // quizId만 있을 때 정렬 조회
    List<QuizOption> findByQuiz_QuizIdOrderByAnswerNumberAsc(Long quizId);

    // 현재 문제의 보기인지 검증
    Optional<QuizOption> findByQuiz_QuizIdAndOptionId(Long quizId, Long optionId);

    // 정답 1개 조회 (answer 응답/검증용)
    Optional<QuizOption> findByQuiz_QuizIdAndIsAnswerTrue(Long quizId);

    // 오답 3개 조회 (explanation용)
    List<QuizOption> findByQuiz_QuizIdAndIsAnswerFalseOrderByAnswerNumberAsc(Long quizId);
}
