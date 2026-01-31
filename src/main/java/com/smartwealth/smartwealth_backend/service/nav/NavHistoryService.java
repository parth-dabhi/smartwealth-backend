package com.smartwealth.smartwealth_backend.service.nav;

import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.nav.PlanNavResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NavHistoryService {
    NavHistoryResponse getNavHistory(Integer planId);
    LatestNavDto getLatestNav(Integer planId);
    Map<Integer, BigDecimal> getLatestNavForPlans(Set<Integer> planIds);
    List<PlanNavResponse> findPlanNavsByDate(LocalDate date);
}
