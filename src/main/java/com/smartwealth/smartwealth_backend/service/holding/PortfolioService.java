package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.dto.response.investment.HoldingTransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PortfolioSummaryResponse;

import java.util.List;

public interface PortfolioService {
    PortfolioSummaryResponse getPortfolio(String customerId);
    PlanPortfolioResponse getPlanPortfolio(String customerId, Integer planId);
    List<HoldingTransactionResponse> getHoldingTransactions(String customerId, Integer planId);
}
