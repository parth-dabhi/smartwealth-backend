package com.smartwealth.smartwealth_backend.repository.investment.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface OrderHistoryProjection {
    Long getInvestmentOrderId();
    String getPlanName();
    String getInvestmentType();
    String getInvestmentMode();
    String getOrderStatus();
    String getPaymentStatus();
    BigDecimal getAmount();
    BigDecimal getUnits();
    BigDecimal getNav();
    LocalDate getNavDate();
    OffsetDateTime getOrderTime();
}
