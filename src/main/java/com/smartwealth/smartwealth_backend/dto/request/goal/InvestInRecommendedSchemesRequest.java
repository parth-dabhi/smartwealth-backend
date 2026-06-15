package com.smartwealth.smartwealth_backend.dto.request.goal;

import com.smartwealth.smartwealth_backend.dto.common.RecommendedLumpsumSchemeDto;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedSipSchemeDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InvestInRecommendedSchemesRequest {

    private List<RecommendedSipSchemeDto> sipSchemes;
    private List<RecommendedLumpsumSchemeDto> lumpsumSchemes;
}
