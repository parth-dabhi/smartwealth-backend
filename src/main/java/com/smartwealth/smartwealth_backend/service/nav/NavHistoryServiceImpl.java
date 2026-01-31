package com.smartwealth.smartwealth_backend.service.nav;

import com.smartwealth.smartwealth_backend.dto.common.NavHistoryPoint;
import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.nav.PlanNavResponse;
import com.smartwealth.smartwealth_backend.exception.nav.NavHistoryNotFoundException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.PlanNotFoundException;
import com.smartwealth.smartwealth_backend.repository.nav.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.nav.projection.LatestNavProjectionWithPlanId;
import com.smartwealth.smartwealth_backend.repository.nav.projection.NavHistoryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public Map<Integer, BigDecimal> getLatestNavForPlans(Set<Integer> planIds) {

        List<LatestNavProjectionWithPlanId> navs =
                navHistoryRepository.findLatestNavByPlanIds(planIds);

        return navs.stream()
                .collect(Collectors.toMap(
                        LatestNavProjectionWithPlanId::getPlanId,
                        LatestNavProjectionWithPlanId::getNavValue
                ));
    }

    @Override
    public List<PlanNavResponse> findPlanNavsByDate(LocalDate date) {
        return navHistoryRepository.findPlanNavsByDate(date)
                .stream().map(PlanNavResponse::fromProjection).toList();
    }
}
