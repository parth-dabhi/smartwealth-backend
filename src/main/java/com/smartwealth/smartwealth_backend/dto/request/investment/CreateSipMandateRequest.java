package com.smartwealth.smartwealth_backend.dto.request.investment;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateSipMandateRequest {

    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be a positive number")
    private Integer planId;

    @NotNull(message = "SIP amount is required")
    @Min(value = 10, message = "SIP amount must be at least 10")
    @Max(value = 1000000000, message = "SIP amount cannot exceed 1,000,000,000")
    @Digits(integer = 10, fraction = 0, message = "SIP amount must be a whole number")
    private BigDecimal sipAmount;

    @NotNull(message = "SIP day is required")
    @Min(value = 1, message = "SIP day must be between 1 and 28")
    @Max(value = 28, message = "SIP day must be between 1 and 28")
    private Integer sipDay;   // 1–28

    @NotNull(message = "Total installments is required")
    @Min(value = 6, message = "Total installments must be at least 6")
    @Max(value = 360, message = "Total installments cannot exceed 360")
    private Integer totalInstallments;
}

