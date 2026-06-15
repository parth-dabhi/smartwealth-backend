package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.response.goal.RecommendationResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.SipRecommendationResponse;

import java.math.BigDecimal;

public interface RecommendationService {

    /**
     * Calculate recommended SIP based on goal target + duration + expected return
     */
    SipRecommendationResponse calculateRecommendedSip(
            Integer durationYears,
            BigDecimal targetAmount,
            Integer riskProfileId
    );

    /**
     * Build scheme recommendation based on
     * user edited SIP + optional lumpsum
     */
    RecommendationResponse generateRecommendation(
            Integer durationYears,
            BigDecimal sipAmount,
            BigDecimal lumpsumAmount,
            Integer riskProfileId
    );
}
