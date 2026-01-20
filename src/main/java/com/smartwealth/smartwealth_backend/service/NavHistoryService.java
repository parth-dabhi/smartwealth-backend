package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;

public interface NavHistoryService {
    NavHistoryResponse getNavHistory(Integer planId);
    LatestNavDto getLatestNav(Integer planId);
}
