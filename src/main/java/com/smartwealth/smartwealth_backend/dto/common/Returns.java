package com.smartwealth.smartwealth_backend.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Returns {

    private String return1y;
    private String return3y;
    private String return5y;
}

