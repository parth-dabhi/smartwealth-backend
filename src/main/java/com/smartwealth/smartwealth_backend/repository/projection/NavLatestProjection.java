package com.smartwealth.smartwealth_backend.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface NavLatestProjection {
    LocalDate getNavDate();
    BigDecimal getNavValue();
}
