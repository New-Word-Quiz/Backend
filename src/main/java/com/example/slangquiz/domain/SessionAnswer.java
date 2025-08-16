package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessionanswer")
@Getter
@NoArgsConstructor
public class SessionAnswer {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // auto_increment
    @Column(name = "answer_id")
    private Long answerId;  // 풀이기록 고유 ID

    @ManyToOne  // QuizSession과 N:1 관계 - 여러 개의 SessionQuiz 레코드가 하나의 QuizSession에 제출
    @JoinColumns({
            @JoinColumn(name = "session_id", referencedColumnName = "session_id", nullable = false),
            @JoinColumn(name = "quiz_id",    referencedColumnName = "quiz_id",    nullable = false)
    })
    private SessionQuiz sessionQuiz;  // 소속 세션

    @ManyToOne  // 선택한 보기와 N:1 관계(복합 FK)
    @JoinColumn(name = "selected_option_id", referencedColumnName = "option_id", nullable = false)
    private QuizOption selectedOption;  // 사용자가 고른 보기 ID

    @Column(name = "is_correct", nullable = false)     // not null
    private Boolean isCorrect;      // 정답 여부 (DB TINYINT(1) -> Boolean)

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;    // 풀이 시각

    //생성자
    public SessionAnswer(SessionQuiz sessionQuiz, QuizOption selectedOption, Boolean isCorrect) {
        this.sessionQuiz = sessionQuiz;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
    }
}
