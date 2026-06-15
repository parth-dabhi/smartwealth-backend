package com.smartwealth.smartwealth_backend.controller.holding;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyPortfolioSummaryResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.HoldingTransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PortfolioSummaryResponse;
import com.smartwealth.smartwealth_backend.service.holding.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.API_PORTFOLIO)
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PortfolioSummaryResponse> getPortfolio(
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(
                portfolioService.getPortfolio(customerId)
        );
    }

    @GetMapping(ApiPaths.PLAN)
    public ResponseEntity<PlanPortfolioResponse> getPlanPortfolio(
            @RequestParam String folioNumber,
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(
                portfolioService.getPlanPortfolio(customerId, folioNumber)
        );
    }

    @GetMapping(ApiPaths.TRANSACTIONS)
    public ResponseEntity<List<HoldingTransactionResponse>> getHoldingTransactions(
            @RequestParam String folioNumber,
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(
                portfolioService.getHoldingTransactions(customerId, folioNumber)
        );
    }

    @GetMapping(ApiPaths.FAMILY_PORTFOLIO)
    public ResponseEntity<FamilyPortfolioSummaryResponse> getFamilyPortfolio(
            @AuthenticationPrincipal String customerId
    ) {
        log.info("Fetching combined family portfolio for customerId={}", customerId);
        return ResponseEntity.ok(
                portfolioService.getFamilyPortfolio(customerId)
        );
    }

    @GetMapping(ApiPaths.FOLIOS)
    public ResponseEntity<List<String>> getFoliosForPlanOfUser(
            @AuthenticationPrincipal String customerId,
            @RequestParam Integer planId
    ) {
        log.info("Fetching folios for customerId={}, planId={}", customerId, planId);
        return ResponseEntity.ok(
                portfolioService.getFoliosForPlanOfUser(customerId, planId)
        );
    }
}
