package com.smartwealth.smartwealth_backend.dto.common;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocation {

    private String assetName;   // Equity, Debt, Hybrid, Commodities
    private BigDecimal allocationPercentage;  // Percentage allocation for the asset class
}
