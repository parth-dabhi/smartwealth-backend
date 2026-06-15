package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedLumpsumSchemeDto {
    private Integer planId;
    private String schemeName;
    private BigDecimal lumpsumAmount;
}
