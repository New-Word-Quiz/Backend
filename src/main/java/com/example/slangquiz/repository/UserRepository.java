package com.example.slangquiz.repository;

import com.example.slangquiz.domain.User;   // User 엔티티
import org.springframework.data.jpa.repository.JpaRepository;   // JPA 기본 CRUD 가능

import java.util.Optional;      // Optional 반환 타입 지원

/*  User 엔티티에 대한 데이터 접근 계층
    User 엔티티의 PK 타입이 Long이므로 제네릭 타입 지정
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 닉네임으로 가장 최근 생성된 유저 찾기
    Optional<User> findFirstByNicknameOrderByUserIdDesc(String nickname);
}
