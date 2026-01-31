package com.smartwealth.smartwealth_backend.repository.nav.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PlanNavByDateProjection {
    Integer getPlanId();
    LocalDate getNavDate();
    BigDecimal getNavValue();
}
