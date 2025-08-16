package com.example.slangquiz.domain;

import jakarta.persistence.*;       // JPA 에너테이션(@Entity, @Id, @Column 등)을 쓰기 위함
import lombok.Getter;               // lombok: getter 메서드 자동생성
import lombok.NoArgsConstructor;    // lombok: 기본 생성자 자동 생성

@Entity                 // 이 클래스는 JPA가 관리하는 엔티티
@Table(name = "user")   // 매핑할 테이블명 암시(ERD와 동일하게)
@Getter                 // Lombok: getter 자동 생성
@NoArgsConstructor      // JPA가 객체를 만들 때 기본 생성자가 필요
public class User {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    @Column(name = "user_id")   // DB 컬렴명과 자바 필드 매핑
    private Long userId;

    @Column(name = "nickname", nullable = false, length = 5)   // not null + 길이 제한 5자
    private String nickname;

    //생성자 (닉네임만으로 생성)
    public User(String nickname) {
        this.nickname = nickname;
    }
}
