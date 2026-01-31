package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AllotmentResult {
    private final int success;
    private final int failed;
    private final String message;

    public static AllotmentResult holiday() {
        return new AllotmentResult(0, 0, "Today is a holiday. No allotments were processed.");
    }

    public static AllotmentResult empty() {
        return new AllotmentResult(0, 0, "No allotments to process.");
    }
}

