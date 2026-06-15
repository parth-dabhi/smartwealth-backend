package com.smartwealth.smartwealth_backend.dto.request.investment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InvestmentSellRequest {

    @NotNull(message = "planId must be provided")
    private Integer planId;

    @NotNull(message = "folioNumber must be provided")
    private String folioNumber;

    /**
     * Sell by amount (₹).
     * Exactly one of amount or units must be provided.
     */
    @DecimalMin(value = "1", inclusive = true)
    private BigDecimal amount;

    /**
     * Sell by units.
     * Exactly one of amount or units must be provided.
     */
    @DecimalMin(value = "0.00000001", inclusive = true)
    private BigDecimal units;
}
