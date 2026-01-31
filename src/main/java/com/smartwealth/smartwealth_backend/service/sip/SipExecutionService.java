package com.smartwealth.smartwealth_backend.service.sip;

import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;

import java.time.OffsetDateTime;

public interface SipExecutionService {

    int executeSingleSip(SipMandate sip, OffsetDateTime now);
}
