package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Returns {

    private String return1y;
    private String return3y;
    private String return5y;
}

