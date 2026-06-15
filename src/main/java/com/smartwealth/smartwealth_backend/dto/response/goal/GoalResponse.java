package com.smartwealth.smartwealth_backend.dto.response.goal;

import com.smartwealth.smartwealth_backend.entity.enums.GoalStatus;
import com.smartwealth.smartwealth_backend.entity.goal.UserGoal;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponse {
    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private Integer durationYears;
    private GoalStatus status;

    public static GoalResponse fromEntity(UserGoal goal) {
        return GoalResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .durationYears(goal.getDurationYears())
                .status(goal.getStatus())
                .build();
    }
}
