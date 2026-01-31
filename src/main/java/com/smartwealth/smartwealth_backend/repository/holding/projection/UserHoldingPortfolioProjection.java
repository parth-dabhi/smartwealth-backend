package com.smartwealth.smartwealth_backend.repository.holding.projection;

import java.math.BigDecimal;

public interface UserHoldingPortfolioProjection {

    Long getHoldingId();
    Integer getPlanId();

    BigDecimal getTotalUnits();
    BigDecimal getTotalInvestedAmount();
    BigDecimal getTotalRedeemedAmount();
    Boolean getIsActive();

    String getPlanName();

    String getAmcName();
    String getAssetName();
    String getCategoryName();
}

