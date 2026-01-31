package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeInfo {

    private Integer schemeId;
    private String schemeName;

    private String amcName;
    private String assetName;
    private String categoryName;
    private String amcWebsite;
}
