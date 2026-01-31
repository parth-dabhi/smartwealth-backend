package com.smartwealth.smartwealth_backend.service.mutual_fund;

import com.smartwealth.smartwealth_backend.dto.response.plan.PlanDetailResponse;

public interface SchemePlanService {
    PlanDetailResponse getPlanDetail(Integer planId);
}
