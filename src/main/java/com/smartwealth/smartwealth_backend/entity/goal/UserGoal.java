package com.smartwealth.smartwealth_backend.entity.goal;

import com.smartwealth.smartwealth_backend.entity.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_goals", indexes = {
        @Index(name = "idx_goal_user", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "goal_name", nullable = false, length = 200)
    private String goalName;

    @Column(name = "target_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @Column(name = "expected_return", nullable = false, precision = 5, scale = 2)
    private BigDecimal expectedReturn;

    @Column(name = "current_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "total_invested", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalInvested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private GoalStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
