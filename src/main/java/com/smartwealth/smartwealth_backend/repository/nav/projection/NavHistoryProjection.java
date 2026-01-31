package com.smartwealth.smartwealth_backend.repository.nav.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface NavHistoryProjection {

    LocalDate getNavDate();
    BigDecimal getNavValue();
}
