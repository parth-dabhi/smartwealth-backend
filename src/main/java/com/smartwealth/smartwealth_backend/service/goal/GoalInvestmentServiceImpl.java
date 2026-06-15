package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentSchemesResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.goal.GoalInvestment;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.repository.goal.GoalInvestmentRepository;
import com.smartwealth.smartwealth_backend.repository.goal.projection.GoalInvestmentProjection;
import com.smartwealth.smartwealth_backend.service.holding.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoalInvestmentServiceImpl implements GoalInvestmentService {

    private final GoalInvestmentRepository goalInvestmentRepository;
    private final PortfolioService portfolioService;

    @Override
    @Transactional
    public void linkInvestmentsBulk(
            Long goalId,
            List<GoalInvestment> investments
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        investments.forEach(gi -> {
            gi.setGoalId(goalId);
            gi.setCreatedAt(now);
        });

        goalInvestmentRepository.saveAll(investments);
    }

    @Override
    public GoalInvestmentSchemesResponse getGoalInvestmentSchemes(
            Long goalId,
            String customerId
    ) {
        List<GoalInvestmentProjection> investments =
                goalInvestmentRepository.findAllByGoalId(goalId);

        if (investments.isEmpty()) {
            return GoalInvestmentSchemesResponse.builder()
                    .investments(Collections.emptyList())
                    .build();
        }

        Set<Long> uniqueHoldingIds = investments.stream()
                .map(GoalInvestmentProjection::getHoldingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PlanPortfolioResponse> result = uniqueHoldingIds.stream()
                .map(holdingId -> {
                    try {
                        // add this method in PortfolioService + PortfolioServiceImpl
                        return portfolioService.getPlanPortfolio(customerId, holdingId);
                    } catch (Exception ex) {
                        log.warn("Skipping invalid holdingId={} for goalId={}", holdingId, goalId);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return GoalInvestmentSchemesResponse.builder()
                .investments(result)
                .build();
    }

    @Override
    public Optional<Long> resolveGoalId(InvestmentOrder order) {
        if (InvestmentMode.SIP.equals(order.getInvestmentMode())
                && order.getSipMandateId() != null) {
            // SIP order → link via sipMandateId
            return goalInvestmentRepository.findGoalIdBySipMandateIdAndPlanId(
                    order.getSipMandateId(),
                    order.getPlanId()
            );
        } else {
            // Lumpsum order → link via investmentOrderId
            return goalInvestmentRepository.findGoalIdByInvestmentOrderIdAndPlanId(
                    order.getInvestmentOrderId(),
                    order.getPlanId()
            );
        }
    }

    @Override
    public void addHoldingIdToGoalInvestment(InvestmentOrder order, Long holdingId) {
        if (InvestmentMode.SIP.equals(order.getInvestmentMode())
                && order.getSipMandateId() != null) {
            // SIP order → update via sipMandateId
            goalInvestmentRepository.updateHoldingIdBySipMandateIdAndPlanId(
                    holdingId,
                    order.getSipMandateId(),
                    order.getPlanId()
            );
        } else {
            // Lumpsum order → update via investmentOrderId
            goalInvestmentRepository.updateHoldingIdByInvestmentOrderIdAndPlanId(
                    holdingId,
                    order.getInvestmentOrderId(),
                    order.getPlanId()
            );
        }
    }
}
