package com.smartwealth.smartwealth_backend.dto.response.plan;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.common.*;
import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.repository.projection.PlanDetailProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanDetailResponse {

    private Integer planId;
    private String planName;

    private Boolean isRecommended;

    private SchemeInfo scheme;

    private String planType;
    private String optionType;

    private Financials financials;
    private Returns returns;
    private Benchmark benchmark;
    private NavSummary nav;

    public static PlanDetailResponse from(
            PlanDetailProjection p,
            LatestNavDto latestNavDto
    ) {

        return PlanDetailResponse.builder()
                .planId(p.getPlanId())
                .planName(p.getPlanName())
                .isRecommended(p.getIsRecommended())
                .scheme(
                        SchemeInfo.builder()
                                .schemeId(p.getSchemeId())
                                .schemeName(p.getSchemeName())
                                .amcName(p.getAmcName())
                                .assetName(p.getAssetName())
                                .categoryName(p.getCategoryName())
                                .build()
                )
                .planType(p.getPlanType())
                .optionType(p.getOptionType())
                .financials(
                        Financials.builder()
                                .expenseRatio(p.getExpenseRatio())
                                .minInvestment(p.getMinInvestment())
                                .minSip(p.getMinSip())
                                .exitLoad(p.getExitLoad())
                                .build()
                )
                .returns(
                        Returns.builder()
                                .return1y(toPercent(p.getReturn1y()))
                                .return3y(toPercent(p.getReturn3y()))
                                .return5y(toPercent(p.getReturn5y()))
                                .build()
                )
                .benchmark(
                        Benchmark.builder()
                                .benchmarkId(p.getBenchmarkId())
                                .benchmarkName(p.getBenchmarkName())
                                .build()
                )
                .nav(
                        latestNavDto == null
                                ? null
                                : NavSummary.builder()
                                    .latestDate(latestNavDto.getNavDate())
                                    .latestValue(latestNavDto.getNavValue())
                                    .historyLink(ApiPaths.API_NAV_HISTORY + "?planId=" + p.getPlanId())
                                    .build()
                )
                .build();
    }

    private static String toPercent(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
