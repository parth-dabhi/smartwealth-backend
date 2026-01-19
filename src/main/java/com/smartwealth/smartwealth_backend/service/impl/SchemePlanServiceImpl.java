package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.response.plan.PlanDetailResponse;
import com.smartwealth.smartwealth_backend.exception.nav.NavHistoryNotFoundException;
import com.smartwealth.smartwealth_backend.exception.plan.PlanNotFoundException;
import com.smartwealth.smartwealth_backend.repository.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.projection.NavLatestProjection;
import com.smartwealth.smartwealth_backend.repository.projection.PlanDetailProjection;
import com.smartwealth.smartwealth_backend.service.SchemePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemePlanServiceImpl implements SchemePlanService {

    private final SchemePlanRepository schemePlanRepository;
    private final NavHistoryRepository navHistoryRepository;

    @Override
    public PlanDetailResponse getPlanDetail(Integer planId) {

        PlanDetailProjection projection = schemePlanRepository.findPlanDetailById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + planId));

        NavLatestProjection latestNavOpt = navHistoryRepository.findLatestNavByPlanId(planId)
                .orElseThrow(() -> new NavHistoryNotFoundException("NAV data not found for plan id: " + planId));

        log.info("Fetched plan detail for planId={}", planId);

        return PlanDetailResponse.from(projection, latestNavOpt);
    }
}
