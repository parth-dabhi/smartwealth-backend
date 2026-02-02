package com.smartwealth.smartwealth_backend.dto.response.family_member;

import com.smartwealth.smartwealth_backend.dto.response.investment.AssetAllocationResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PortfolioHoldingResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndividualPortfolioResponse {

    // Owner identification
    private String ownerName;
    private Boolean isPersonal; // true = own portfolio, false = family member's

    // Portfolio metrics
    private BigDecimal totalNetInvestedAmount;
    private BigDecimal totalMarketValue;
    private BigDecimal totalNetGain;
    private String totalNetGainPercentage;

    // Asset allocation for this individual
    private List<AssetAllocationResponse> assetAllocation;

    // Holdings for this individual
    private List<PortfolioHoldingResponse> holdings;
}
