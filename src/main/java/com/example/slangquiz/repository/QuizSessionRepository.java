package com.example.slangquiz.repository;

import com.example.slangquiz.domain.QuizSession;    // QuizSession 엔티티
import com.example.slangquiz.domain.User;           // User 엔티티
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// QuizSession 엔티티용 Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    // 특정 유저의 가장 최근 세션 조회
    Optional<QuizSession> findTopByUserOrderBySessionIdDesc(User user);

    // 특정 유저의 모든 세션을 최신순으로 조회
    List<QuizSession> findByUserOrderBySessionIdDesc(User user);
}
