package com.smartwealth.smartwealth_backend.repository.mutual_fund.projection;

public interface PlanProjection {
    Integer getPlanId();
    Integer getSchemeId();

    String getPlanType();
    String getOptionType();
}
