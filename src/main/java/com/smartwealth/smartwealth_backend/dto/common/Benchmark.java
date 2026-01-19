package com.smartwealth.smartwealth_backend.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Benchmark {

    private Integer benchmarkId;
    private String benchmarkName;
}

