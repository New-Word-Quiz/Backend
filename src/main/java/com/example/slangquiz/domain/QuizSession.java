package com.example.slangquiz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; // createdAt 필드 타입(DB에서의 DATETIME)

@Entity
@Table(name = "quizsession")
@Getter
@NoArgsConstructor
public class QuizSession {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // auto_increment
    @Column(name = "session_id")
    private Long sessionId;  // 보기 고유 ID

    @ManyToOne  // User와 N:1 관계 - 여러 세션이 하나의 유저에 속함
    @JoinColumn(name = "user_id", nullable = false)     // not null
    private User user;  // 세션 소유 유저

    @Column(name = "score")     // null 허용(게임 종료 후 채워질 수 있음)
    private Integer score;      // 이번 세션의 점수 (0~100, 5점 단위)

    // not null, 이후 update로 변경 x
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;    // 세션 생성 시각

    //생성자
    public QuizSession(User user) {
        this.user = user;
    }

    //점수만 변경하는 세터 : fianlizeResult 에서 사용
    public void setScore(Integer score) {
        this.score = score;
    }
}
