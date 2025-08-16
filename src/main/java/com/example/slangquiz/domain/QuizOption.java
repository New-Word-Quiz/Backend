package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quizoption",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"quiz_id", "answer_number"}),  // 한 퀴즈 내 보기 순서 (1~4) 중복 금지
            @UniqueConstraint(columnNames = {"quiz_id", "option_id"})       // 복합 FK 보장 (SessionAnswer에서 사용)
        }
)
@Getter
@NoArgsConstructor
public class QuizOption {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // auto_increment
    @Column(name = "option_id")
    private Long optionId;  // 보기 고유 ID

    @ManyToOne  // Quiz와 N:1 관계 - 여러 개의 보기는 한
    @JoinColumn(name = "quiz_id", nullable = false)     // not null
    private Quiz quiz;  // 소속 퀴즈

    @Column(name = "option_text", nullable = false)     // not null
    private String optionText;      // 보기 텍스트

    @Column(name = "option_meaning", nullable = false)  // not null
    private String optionMeaning;     // 보기 뜻

    @Column(name = "example_text")  // null 허용 (정답 보기일 때만 제공)
    private String exampleText;     // 예문

    @Column(name = "answer_number", nullable = false)  // not null
    private Integer answerNumber;   // 보기 순서 (1~4)

    @Column(name = "is_answer", nullable = false)  // not null
    private Boolean isAnswer;    // 정답 여부(DB의 TINYINT(1) -> Boolean)

    //생성자(필수값만)
    public QuizOption(Quiz quiz,
                      String optionText,
                      String optionMeaning,
                      Integer answerNumber,
                      Boolean isAnswer) {
        this.quiz = quiz;
        this.optionText = optionText;
        this.optionMeaning = optionMeaning;
        this.answerNumber = answerNumber;
        this.isAnswer = isAnswer;
    }

    //생성자(exampleText 포함)
    public QuizOption(Quiz quiz,
                      String optionText,
                      String optionMeaning,
                      String exampleText,
                      Integer answerNumber,
                      Boolean isAnswer) {
        this.quiz = quiz;
        this.optionText = optionText;
        this.optionMeaning = optionMeaning;
        this.exampleText = exampleText;
        this.answerNumber = answerNumber;
        this.isAnswer = isAnswer;
    }
}
