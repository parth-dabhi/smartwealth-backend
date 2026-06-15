package com.smartwealth.smartwealth_backend.dto.response.goal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class SipRecommendationResponse {

    private BigDecimal recommendedSip;
}

