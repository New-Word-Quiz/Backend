package com.example.slangquiz.repository;

import com.example.slangquiz.domain.Hint;   // Hint 엔티티
import com.example.slangquiz.domain.Quiz;   // Quiz 엔티티
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Hint 엔티티용 Repository
public interface HintRepository extends JpaRepository<Hint, Long> {

    // 엔티티로 조회
    Optional<Hint> findByQuiz(Quiz quiz);

    // quizId만 있을 때 편의 메서드
    Optional<Hint> findByQuiz_QuizId(Long quizId);
}
