package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentSchemesResponse;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.goal.GoalInvestment;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;

import java.util.List;
import java.util.Optional;

public interface GoalInvestmentService {

    void linkInvestmentsBulk(Long goalId, List<GoalInvestment> investments);
    GoalInvestmentSchemesResponse getGoalInvestmentSchemes(Long goalId, String customerId);
    Optional<Long> resolveGoalId(InvestmentOrder order);
    void addHoldingIdToGoalInvestment(InvestmentOrder order, Long holdingId);
}
