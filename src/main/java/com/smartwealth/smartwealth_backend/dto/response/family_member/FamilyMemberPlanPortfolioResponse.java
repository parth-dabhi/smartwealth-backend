package com.smartwealth.smartwealth_backend.dto.response.family_member;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyMemberPlanPortfolioResponse {

    // Owner information
    private String ownerCustomerId;
    private String ownerName;

    // Plan details
    private Integer planId;
    private String planName;

    private String amcName;
    private String assetName;
    private String categoryName;

    private BigDecimal investedAmount;
    private BigDecimal marketValue;
    private BigDecimal gain;
    private BigDecimal units;

    private BigDecimal latestNav;
    private LocalDate navDate;

    private Boolean isActive;
}
