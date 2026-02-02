package com.smartwealth.smartwealth_backend.dto.response.family_member;

import com.smartwealth.smartwealth_backend.dto.response.investment.AssetAllocationResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyPortfolioSummaryResponse {

    // Combined totals across all portfolios
    private BigDecimal totalNetInvestedAmount;
    private BigDecimal totalMarketValue;
    private BigDecimal totalNetGain;
    private String totalNetGainPercentage;

    // Combined asset allocation
    private List<AssetAllocationResponse> assetAllocation;

    // Individual portfolio breakdown
    private IndividualPortfolioResponse personalPortfolio;
    private List<IndividualPortfolioResponse> familyMemberPortfolios;
}
