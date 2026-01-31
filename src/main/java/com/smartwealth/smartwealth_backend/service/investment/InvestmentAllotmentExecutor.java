package com.smartwealth.smartwealth_backend.service.investment;

import com.smartwealth.smartwealth_backend.dto.common.AllotmentResult;
import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentOrderRepository;
import com.smartwealth.smartwealth_backend.repository.nav.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import com.smartwealth.smartwealth_backend.service.common.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentAllotmentExecutor {

    private final InvestmentAllotmentService investmentAllotmentService;
    private final InvestmentOrderRepository investmentOrderRepository;
    private final NavHistoryRepository navHistoryRepository;
    private final TradingHolidayService tradingHolidayService;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public AllotmentResult processOrdersByStatus(OrderStatus status) {

        if (tradingHolidayService.isHoliday(LocalDate.now(IST))) {
            log.info("Trading holiday. Skipping allotment for status={}", status);
            return AllotmentResult.holiday();
        }

        LocalDate today = LocalDate.now(IST);

        List<InvestmentOrder> orders =
                investmentOrderRepository.findByStatus(status, today);

        if (orders.isEmpty()) {
            log.info("No orders found for status={}", status);
            return AllotmentResult.empty();
        }

        log.info("Processing {} investment orders for allotment", orders.size());

        int success = 0;
        int failed = 0;

        // Group orders by applicable NAV date
        Map<LocalDate, List<InvestmentOrder>> ordersByNavDate =
                orders.stream()
                        .collect(Collectors.groupingBy(
                                InvestmentOrder::getApplicableNavDate
                        ));

        for (var entry : ordersByNavDate.entrySet()) {

            LocalDate navDate = entry.getKey();
            List<InvestmentOrder> dateOrders = entry.getValue();

            // Collect unique planIds for this NAV date
            Set<Integer> planIds = dateOrders.stream()
                    .map(InvestmentOrder::getPlanId)
                    .collect(Collectors.toSet());

            // Fetch NAVs for this date
            Map<Integer, PlanNavProjection> navMap =
                    navHistoryRepository.findNavsByDateAndPlanIds(navDate, planIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    PlanNavProjection::getPlanId,
                                    nav -> nav
                            ));

            // Process orders for this NAV date
            for (InvestmentOrder order : dateOrders) {

                try {
                    PlanNavProjection nav = navMap.get(order.getPlanId());

                    if (nav == null) {
                        log.info(
                                "NAV not available yet. orderId={}, planId={}, navDate={}",
                                order.getInvestmentOrderId(),
                                order.getPlanId(),
                                navDate
                        );
                        order.setStatus(OrderStatus.NAV_PENDING);
                        order.setUpdatedAt(OffsetDateTime.now(IST));
                        investmentOrderRepository.save(order);
                        continue;
                    }

                    // Process single order
                    success += investmentAllotmentService.processSingleOrder(order, nav);

                } catch (Exception ex) {
                    // Log the error but continue processing remaining orders
                    log.error(
                            "Allotment failed. orderId={}, status={}",
                            order.getInvestmentOrderId(),
                            status,
                            ex
                    );
                    failed++;
                }
            }
        }

        log.info(
                "Scheduled Investment Allotment completed. allotted: {}, Failed: {}",
                success,
                failed
        );
        return new AllotmentResult(success, failed, "Allotment processed for status=" + status);
    }
}
