package com.smartwealth.smartwealth_backend.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SipSuspendedEvent {
    private final Long sipMandateId;
    private final Long userId;
    private final Integer newFailureCount;
}
