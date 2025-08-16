package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hint")
@Getter
@NoArgsConstructor
public class Hint {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    @Column(name = "hint_id")
    private Long hintId;    // 힌트 고유 ID

    @OneToOne   // Quiz와 1:1 관계
    @JoinColumn(name = "quiz_id", nullable = false, unique = true)  // 외래키 컬럼 이름/제약 명시
    private Quiz quiz;

    @Column(name = "hint_text", nullable = false)   // not null
    private String hintText;    // 힌트 내용

    @Column(name = "example_text", nullable = false)    // not null
    private String exampleText;     // 힌트 예문

    //생성자
    public Hint(Quiz quiz, String hintText, String exampleText) {
        this.quiz = quiz;
        this.hintText = hintText;
        this.exampleText = exampleText;
    }
}
