package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LumpsumBasketResult {
    private List<RecommendedLumpsumSchemeDto> basketA;
    private List<RecommendedLumpsumSchemeDto> basketB;
    private List<RecommendedLumpsumSchemeDto> basketC;
}
