package com.smartwealth.smartwealth_backend.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchemeInfo {

    private Integer schemeId;
    private String schemeName;

    private String amcName;
    private String assetName;
    private String categoryName;
}
