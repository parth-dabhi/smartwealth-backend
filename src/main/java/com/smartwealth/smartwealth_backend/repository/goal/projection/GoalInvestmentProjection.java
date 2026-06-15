package com.smartwealth.smartwealth_backend.repository.goal.projection;

public interface GoalInvestmentProjection {

    Long getSipMandateId();
    Long getInvestmentOrderId();
    Integer getPlanId();
    Long getHoldingId();
}
