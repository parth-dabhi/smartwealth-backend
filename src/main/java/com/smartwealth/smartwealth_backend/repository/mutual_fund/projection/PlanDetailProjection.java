package com.smartwealth.smartwealth_backend.repository.mutual_fund.projection;

import java.math.BigDecimal;

public interface PlanDetailProjection {

    Integer getPlanId();
    String getPlanName();
    String getPlanType();
    String getOptionType();
    Boolean getIsRecommended();

    Integer getSchemeId();
    String getSchemeName();
    String getAmcName();
    String getAssetName();
    String getCategoryName();
    String getWebsite();

    BigDecimal getExpenseRatio();
    BigDecimal getMinInvestment();
    BigDecimal getMinSip();
    Boolean getIsSipAllowed();
    String getExitLoad();

    BigDecimal getReturn1y();
    BigDecimal getReturn3y();
    BigDecimal getReturn5y();

    Integer getBenchmarkId();
    String getBenchmarkName();
}
