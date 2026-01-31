package com.smartwealth.smartwealth_backend.repository.nav.projection;

import java.math.BigDecimal;

public interface LatestNavProjectionWithPlanId {
    Integer getPlanId();
    BigDecimal getNavValue();
}
