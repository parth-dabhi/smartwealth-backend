package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;

public interface NavHistoryService {
    NavHistoryResponse getNavHistory(Integer planId);
}
