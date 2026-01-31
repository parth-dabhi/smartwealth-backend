package com.smartwealth.smartwealth_backend.scheduler;

import com.smartwealth.smartwealth_backend.dto.common.AllotmentResult;
import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import com.smartwealth.smartwealth_backend.service.investment.InvestmentAllotmentExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class NavPendingAllotmentScheduler {

    private final InvestmentAllotmentExecutor investmentAllotmentExecutor;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Now works via manual trigger as service class
//    @Scheduled(cron = "0 0 */2 * * *", zone = "Asia/Kolkata")
    public String retryNavPendingOrders() {
        AllotmentResult result =
                investmentAllotmentExecutor.processOrdersByStatus(OrderStatus.PENDING);

        return "Allotted: " + result.getSuccess()
                + ", Failed: " + result.getFailed();
    }
}
