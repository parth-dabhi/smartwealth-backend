package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@AllArgsConstructor
@ToString
public class AssetMix {

    private BigDecimal equityPercent;
    private BigDecimal debtPercent;

    public AssetMix(int equityPercent, int debtPercent) {
        this.equityPercent = BigDecimal.valueOf(equityPercent);
        this.debtPercent = BigDecimal.valueOf(debtPercent);
    }

    public BigDecimal getEquityAmount(BigDecimal total) {
        return total.multiply(equityPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    public BigDecimal getDebtAmount(BigDecimal total) {
        return total.multiply(debtPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }
}
