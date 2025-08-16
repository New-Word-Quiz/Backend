package com.example.slangquiz.repository;

import com.example.slangquiz.domain.QuizSession;    // QuizSession 엔티티
import com.example.slangquiz.domain.SessionQuiz;    // SessionQuiz 엔티티
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


// SessionQuiz 엔티티용 Repository
public interface SessionQuizRepository extends JpaRepository<SessionQuiz, Long> {

    // 특정 세션의 퀴즈 전체/특정 순번 조회
    List<SessionQuiz> findBySessionOrderByQuizOrderAsc(QuizSession session);
    Optional<SessionQuiz> findBySessionAndQuizOrder(QuizSession session, Integer quizOrder);

    // 현재 세션에서 아직 제출하지 않은 문제 중에서 세션 내 순서가 가장 작은 항목을 찾음
    @Query("""
    SELECT sq
    FROM SessionQuiz sq
    WHERE sq.session = :session
      AND NOT EXISTS (
        SELECT 1
        FROM SessionAnswer sa
        WHERE sa.sessionQuiz = sq
      )
    ORDER BY sq.quizOrder ASC
""")
    List<SessionQuiz> findNextUnanswered(@Param("session") QuizSession session, Pageable pageable);
}
