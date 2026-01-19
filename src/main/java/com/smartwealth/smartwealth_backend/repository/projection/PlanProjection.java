package com.smartwealth.smartwealth_backend.repository.projection;

public interface PlanProjection {
    Integer getPlanId();
    Integer getSchemeId();

    String getPlanType();
    String getOptionType();
}
