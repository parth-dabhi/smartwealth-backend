package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummaryResponse {

    private BigDecimal totalNetInvestedAmount;
    private BigDecimal totalMarketValue;
    private BigDecimal totalNetGain;
    private String totalNetGainPercentage;

    private List<AssetAllocationResponse> assetAllocation;
    private List<PortfolioHoldingResponse> holdings;
}
