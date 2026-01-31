package com.smartwealth.smartwealth_backend.controller.mutual_fund;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.plan.PlanDetailResponse;
import com.smartwealth.smartwealth_backend.service.mutual_fund.SchemePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_PLANS)
@RequiredArgsConstructor
@Slf4j
public class SchemePlanController {
    private final SchemePlanService schemePlanService;

    @GetMapping(ApiPaths.PLAN_ID)
    public ResponseEntity<PlanDetailResponse> getPlanDetail(@PathVariable Integer planId) {
        log.debug("Fetching plan detail for planId={}", planId);
        PlanDetailResponse response = schemePlanService.getPlanDetail(planId);
        return ResponseEntity.ok(response);
    }
}
