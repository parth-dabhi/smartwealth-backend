package com.smartwealth.smartwealth_backend.repository.sip.projection;

import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;

public interface SipResumeProjection {
    SipStatus getStatus();
    Integer getSipDay();
}
