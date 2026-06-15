package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.request.goal.CreateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.InvestInRecommendedSchemesRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.UpdateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.response.goal.DetailGoalResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentResult;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentSchemesResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalResponse;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;

import java.math.BigDecimal;
import java.util.List;

public interface UserGoalService {

    GoalResponse createGoal(
            String customerId,
            CreateGoalRequest request
    );

    List<GoalResponse> getUserGoals(
            String customerId
    );

    DetailGoalResponse getGoal(
            String customerId,
            Long goalId
    );

    GoalResponse updateGoal(
            String customerId,
            Long goalId,
            UpdateGoalRequest request
    );

    GoalInvestmentResult investInGoal(
            String customerId,
            Long goalId,
            InvestInRecommendedSchemesRequest request
    );

    void updateGoalOnAllotment(
            InvestmentOrder order,
            BigDecimal amount
    );
}
