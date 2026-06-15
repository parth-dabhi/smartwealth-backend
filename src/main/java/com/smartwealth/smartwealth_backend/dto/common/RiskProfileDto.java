package com.smartwealth.smartwealth_backend.dto.common;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiskProfileDto {

    private Integer id;
    private String name;
    private Integer level;

    private BigDecimal equityPercent;
    private BigDecimal debtPercent;
    private BigDecimal hybridPercent;
    private BigDecimal commoditiesPercent;
}
