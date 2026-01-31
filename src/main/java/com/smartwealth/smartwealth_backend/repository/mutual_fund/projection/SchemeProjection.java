package com.smartwealth.smartwealth_backend.repository.mutual_fund.projection;

public interface SchemeProjection {
    Integer getSchemeId();
    String getSchemeName();

    String getAmcName();
    String getAssetName();
    String getCategoryName();
}
