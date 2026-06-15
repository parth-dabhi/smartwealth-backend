package com.smartwealth.smartwealth_backend.dto.response.goal;

import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import com.smartwealth.smartwealth_backend.entity.enums.GoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailGoalResponse {

    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private Integer durationYears;
    private BigDecimal currentValue;
    private BigDecimal totalInvested;
    private String progress;
    private GoalStatus status;

    private List<PlanPortfolioResponse> investments;
}
