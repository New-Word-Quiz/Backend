package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor
public class Quiz {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    @Column(name = "quiz_id")
    private Long quizId;    // 퀴즈 고유 ID

    @Column(name = "quiz_text", nullable = false)   // not null
    private String quizText;        // 퀴즈 본문 내용

    @Column(name = "image_url")     // null 허용
    private String imageUrl;        // 이미지 URL

    //생성자 (본문 + 이미지 URL을 함께 세팅하고 싶을때
    public Quiz(String quizText, String imageUrl) {
        this.quizText = quizText;
        this.imageUrl = imageUrl;
    }

    //생성자 (필수값 quizText만 받음)
    public Quiz(String quizText) {
        this.quizText = quizText;
    }
}
