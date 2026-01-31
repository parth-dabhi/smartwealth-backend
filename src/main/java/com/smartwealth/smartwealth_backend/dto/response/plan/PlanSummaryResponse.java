package com.smartwealth.smartwealth_backend.dto.response.plan;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.PlanProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
