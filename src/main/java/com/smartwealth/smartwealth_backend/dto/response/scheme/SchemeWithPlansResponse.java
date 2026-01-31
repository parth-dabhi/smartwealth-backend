package com.smartwealth.smartwealth_backend.dto.response.scheme;

import com.smartwealth.smartwealth_backend.dto.response.plan.PlanSummaryResponse;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.PlanProjection;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.SchemeProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeWithPlansResponse {

    // scheme level details
    private Integer schemeId;
    private String schemeName;

    private String amcName;
    private String assetName;
    private String categoryName;

    // list of plans under the scheme
    private List<PlanSummaryResponse> plans;

    public static SchemeWithPlansResponse from(SchemeProjection scheme, List<PlanProjection> plans) {
        return SchemeWithPlansResponse.builder()
                .schemeId(scheme.getSchemeId())
                .schemeName(scheme.getSchemeName())
                .amcName(scheme.getAmcName())
                .assetName(scheme.getAssetName())
                .categoryName(scheme.getCategoryName())
                .plans(
                        plans.stream()
                                .map(PlanSummaryResponse::from)
                                .toList()
                )
                .build();
    }
}
