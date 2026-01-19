package com.smartwealth.smartwealth_backend.repository.projection;

public interface SchemeProjection {
    Integer getSchemeId();
    String getSchemeName();

    String getAmcName();
    String getAssetName();
    String getCategoryName();
}
