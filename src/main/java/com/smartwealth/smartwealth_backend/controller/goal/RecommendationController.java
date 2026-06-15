package com.smartwealth.smartwealth_backend.controller.goal;

import com.smartwealth.smartwealth_backend.dto.response.goal.RecommendationResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.SipRecommendationResponse;
import com.smartwealth.smartwealth_backend.service.goal.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService goalRecommendationService;

    /**
     * Get recommended SIP amount
     */
    @GetMapping("/sip-recommendation")
    public ResponseEntity<SipRecommendationResponse> getRecommendedSip(
            @RequestParam Integer durationYears,
            @RequestParam BigDecimal targetAmount,
            @RequestParam Integer riskProfileId
    ) {

        SipRecommendationResponse response = goalRecommendationService.calculateRecommendedSip(
                durationYears,
                targetAmount,
                riskProfileId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Generate scheme recommendation
     */
    @GetMapping("/scheme-recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendation(
            @RequestParam Integer durationYears,
            @RequestParam BigDecimal sipAmount,
            @RequestParam(required = false) BigDecimal lumpsumAmount,
            @RequestParam Integer riskProfileId
    ) {

        RecommendationResponse response =
                goalRecommendationService.generateRecommendation(
                        durationYears,
                        sipAmount,
                        lumpsumAmount != null ? lumpsumAmount : BigDecimal.ZERO,
                        riskProfileId
                );

        return ResponseEntity.ok(response);
    }
}
