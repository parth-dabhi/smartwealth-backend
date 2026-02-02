package com.smartwealth.smartwealth_backend.dto.common;

import com.smartwealth.smartwealth_backend.dto.response.investment.AssetAllocationResponse;

import java.math.BigDecimal;
import java.util.List;

public record CombinedTotals(
        BigDecimal totalInvested,
        BigDecimal totalMarketValue,
        BigDecimal totalGain,
        String gainPercentage,
        List<AssetAllocationResponse> assetAllocation
) {}
