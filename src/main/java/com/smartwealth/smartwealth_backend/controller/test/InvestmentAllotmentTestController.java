package com.smartwealth.smartwealth_backend.controller.test;

import com.smartwealth.smartwealth_backend.scheduler.InvestmentAllotmentScheduler;
//import com.smartwealth.smartwealth_backend.scheduler.NavPendingAllotmentScheduler;
import com.smartwealth.smartwealth_backend.scheduler.NavPendingAllotmentScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/investment-allotment")
@RequiredArgsConstructor
public class InvestmentAllotmentTestController {

    private final InvestmentAllotmentScheduler allotmentScheduler; // Temporary use of scheduler, working as a service
    private final NavPendingAllotmentScheduler navPendingAllotmentScheduler;

    @PostMapping("/run-pending")
    public ResponseEntity<String> runInvestmentAllotment() {
        return ResponseEntity.ok(allotmentScheduler.allotPendingOrders());
    }

    @PostMapping("/run-nav-pending")
    public String runNavPendingAllotment() {
        return navPendingAllotmentScheduler.retryNavPendingOrders();
    }
}
