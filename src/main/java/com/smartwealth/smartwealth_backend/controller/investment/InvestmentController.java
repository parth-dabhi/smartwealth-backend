package com.smartwealth.smartwealth_backend.controller.investment;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentBuyRequest;
import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentSellRequest;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentBuyResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentSellResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.OrderHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.service.investment.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.API_INVESTMENT)
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping(ApiPaths.LUMPSUM) // BUY endpoint
    public InvestmentBuyResponse invest(
            @Valid @RequestBody InvestmentBuyRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal String customerId
    ) {
        return investmentService.buy(request, customerId, idempotencyKey);
    }

    @PostMapping(ApiPaths.SELL) // SELL endpoint
    public InvestmentSellResponse sell(
            @Valid @RequestBody InvestmentSellRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal String customerId
    ) {
        return investmentService.sell(request, customerId, idempotencyKey);
    }

    @GetMapping(ApiPaths.ORDER_HISTORY)
    public ResponseEntity<PaginationResponse<OrderHistoryResponse>> getOrderHistory(
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(
                investmentService.getOrderHistory(customerId)
        );
    }
}