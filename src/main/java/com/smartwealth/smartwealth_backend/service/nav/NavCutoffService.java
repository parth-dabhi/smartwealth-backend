package com.smartwealth.smartwealth_backend.service.nav;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface NavCutoffService {
    /**
     * Calculates applicable NAV date for an investment order.
     *
     * @param orderTime IST time of order placement
     * @return applicable NAV date
     */
    LocalDate calculateApplicableNavDate(Integer planId, InvestmentType transactionType, OffsetDateTime orderTime);
}
