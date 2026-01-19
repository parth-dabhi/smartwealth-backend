package com.smartwealth.smartwealth_backend.dto.response.plan;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.repository.projection.PlanProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanSummaryResponse {
    private Integer planId;
    private String planType;
    private String optionType;
    private String detailLink;

    public static PlanSummaryResponse from(PlanProjection plan) {
        return PlanSummaryResponse.builder()
                .planId(plan.getPlanId())
                .planType(plan.getPlanType())
                .optionType(plan.getOptionType())
                .detailLink(ApiPaths.API_PLANS + "/" + plan.getPlanId())
                .build();
    }
}
