package com.smartwealth.smartwealth_backend.dto.request.investment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InvestmentBuyRequest {

    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be a positive number")
    private Integer planId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    private BigDecimal amount;
}

