package com.smartwealth.smartwealth_backend.dto.common;

import com.smartwealth.smartwealth_backend.dto.response.investment.AssetAllocationResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PortfolioHoldingResponse;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioData(
        BigDecimal totalNetInvestedAmount,
        BigDecimal totalMarketValue,
        BigDecimal totalNetGain,
        String totalNetGainPercentage,
        List<AssetAllocationResponse> assetAllocations,
        List<PortfolioHoldingResponse> holdingResponses
) {}
