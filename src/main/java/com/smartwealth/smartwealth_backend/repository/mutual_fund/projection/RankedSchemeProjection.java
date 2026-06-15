package com.smartwealth.smartwealth_backend.repository.mutual_fund.projection;

import java.math.BigDecimal;

public interface RankedSchemeProjection {

    Integer getPlanId();
    String getPlanName();
    BigDecimal getReturn5y();
    Integer getCategoryId();
    String getCategoryName();
}
