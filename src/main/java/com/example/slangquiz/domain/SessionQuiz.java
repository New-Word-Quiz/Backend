package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "sessionquiz",          // ✅ "SessionQuiz" → "sessionquiz"
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "quiz_order"}),  // 동일 순번 중복 금지
                @UniqueConstraint(columnNames = {"session_id", "quiz_id"})      // 같은 문제 재삽입 금지
        }
)
@Getter
@NoArgsConstructor
public class SessionQuiz {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // auto_increment
    @Column(name = "session_quiz_id")
    private Long sessionQuizId;  // 세션-퀴즈 연결 고유 ID

    @ManyToOne  // QuizSession과 N:1 관계 - 여러 개의 SessionQuiz 레코드가 하나의 QuizSession에 제출
    @JoinColumn(name = "session_id", nullable = false)     // not null
    private QuizSession session;  // 소속 퀴즈

    @ManyToOne  // Quiz와 N:1 관계 - 같은 퀴즈가 여러 세션에 재사용 가능
    @JoinColumn(name = "quiz_id", nullable = false)     // not null
    private Quiz quiz;

    @Column(name = "quiz_order", nullable = false)     // not null
    private Integer quizOrder;      // 세션 내 순서(1~20)

    //생성자
    public SessionQuiz(QuizSession session, Quiz quiz, Integer quizOrder) {
        this.session = session;
        this.quiz = quiz;
        this.quizOrder = quizOrder;
    }
}
