package com.smartwealth.smartwealth_backend.repository.holding.projection;

import java.math.BigDecimal;

public interface UserHoldingSellProjection {
    BigDecimal getTotalUnits();
    Boolean getIsActive();
}
