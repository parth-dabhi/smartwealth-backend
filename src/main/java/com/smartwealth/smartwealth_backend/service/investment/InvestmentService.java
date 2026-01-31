package com.smartwealth.smartwealth_backend.service.investment;

import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentBuyRequest;
import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentSellRequest;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentBuyResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentSellResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.OrderHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;

public interface InvestmentService {
    InvestmentBuyResponse buy(InvestmentBuyRequest request, String customerId, String idempotencyKey);
    InvestmentSellResponse sell(InvestmentSellRequest request, String customerId, String idempotencyKey);
    PaginationResponse<OrderHistoryResponse> getOrderHistory(String customerId);
}
