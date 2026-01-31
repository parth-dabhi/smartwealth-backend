package com.smartwealth.smartwealth_backend.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class SipExecutionFailedEvent {
    private Long sipMandateId;
    private Long userId;
    private OffsetDateTime failedAt;
    private Exception ex;
}
