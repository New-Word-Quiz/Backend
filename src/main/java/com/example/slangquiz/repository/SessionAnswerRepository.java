package com.example.slangquiz.repository;

import com.example.slangquiz.domain.QuizSession;    // QuizSession 엔티티
import com.example.slangquiz.domain.SessionAnswer;    // SessionAnswer 엔티티
import com.example.slangquiz.domain.SessionQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// SessionAnswer 엔티티용 Repository
public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {

    // 특정 세션의 모든 풀이 기록을 순서대로 조회
    List<SessionAnswer> findBySessionQuiz_SessionOrderByAnswerIdAsc(QuizSession session);

    // 정답 수 계산/진행도
    long countBySessionQuiz_Session(QuizSession session);

    // 정답 수 카운트(점수 = 정답 수 x 5)
    long countBySessionQuiz_SessionAndIsCorrectTrue(QuizSession session);

    // 중복 제출 방지
    boolean existsBySessionQuiz(SessionQuiz sessionQuiz);

    // 가장 최근 제출
    Optional<SessionAnswer> findTopBySessionQuiz_SessionOrderByCreatedAtDesc(QuizSession session);
}
