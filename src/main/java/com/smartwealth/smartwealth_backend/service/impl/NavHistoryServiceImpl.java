package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.NavHistoryPoint;
import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;
import com.smartwealth.smartwealth_backend.exception.nav.NavHistoryNotFoundException;
import com.smartwealth.smartwealth_backend.exception.plan.PlanNotFoundException;
import com.smartwealth.smartwealth_backend.repository.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.projection.NavHistoryProjection;
import com.smartwealth.smartwealth_backend.repository.projection.NavLatestProjection;
import com.smartwealth.smartwealth_backend.service.NavHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavHistoryServiceImpl implements NavHistoryService {

    private final NavHistoryRepository navHistoryRepository;
    private final SchemePlanRepository schemePlanRepository;

    @Override
    @Cacheable(
            value = "navHistory",
            key = "#planId",
            unless = "#result == null"
    )
    public NavHistoryResponse getNavHistory(Integer planId) {

        if (!schemePlanRepository.existsByPlanId(planId)) {
            throw new PlanNotFoundException("Scheme plan not found for planId: " + planId);
        }

        List<NavHistoryProjection> navs = navHistoryRepository.findAllNavByPlanId(planId)
                .orElseThrow(() -> new NavHistoryNotFoundException("No NAV history found for planId: " + planId));

        List<NavHistoryPoint> points =
                navs.stream()
                        .map(n -> new NavHistoryPoint(
                                n.getNavDate(),
                                n.getNavValue()
                        ))
                        .toList();

        log.info("Retrieved {} NAV history points for planId={}", points.size(), planId);

        return NavHistoryResponse.builder()
                .planId(planId)
                .navs(points)
                .build();
    }

    @Override
    @Cacheable(
            value = "latestNav",
            key = "#planId",
            unless = "#result == null"
    )
    public LatestNavDto getLatestNav(Integer planId) {
        log.debug("Latest NAV cache MISS for planId={}", planId);
        return navHistoryRepository.findLatestNavByPlanId(planId)
                .map(p -> LatestNavDto.builder()
                        .navDate(p.getNavDate())
                        .navValue(p.getNavValue())
                        .build()
                )
                .orElse(null);
    }
}
