package com.example.slangquiz.repository;

import com.example.slangquiz.domain.Quiz;   // Quiz 엔티티
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Quiz 엔티티용 Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 세션 생성 시 20문제 랜덤 추출용
    @Query(value = "SELECT * FROM quiz ORDER BY RAND() LIMIT :n", nativeQuery = true)
    List<Quiz> pickRandom(@Param("n") int n);
}
