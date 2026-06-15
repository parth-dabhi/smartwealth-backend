package com.smartwealth.smartwealth_backend.repository.mutual_fund.projection;

import java.math.BigDecimal;

public interface TopSipPlanProjection extends TopPlanProjection {

    BigDecimal getMinSipAmount();
}
