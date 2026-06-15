package com.smartwealth.smartwealth_backend.dto.response.goal;

import com.smartwealth.smartwealth_backend.dto.common.AssetAllocation;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedLumpsumSchemeDto;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedSipSchemeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponse {

    private BigDecimal sipAmount;
    private BigDecimal lumpsumAmount;

    private List<AssetAllocation> assetAllocation;

    private List<RecommendedSipSchemeDto> sipSchemes;
    private List<RecommendedLumpsumSchemeDto> lumpsumSchemes;
}
