package com.smartwealth.smartwealth_backend.service.sip;

import com.smartwealth.smartwealth_backend.dto.request.investment.CreateSipMandateRequest;
import com.smartwealth.smartwealth_backend.dto.response.investment.ListAllSipMandateResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.SipMandateResponse;

public interface SipMandateService {
    SipMandateResponse createSip(String customerId, CreateSipMandateRequest request);
    ListAllSipMandateResponse getAllUserSips(String customerId);
    String pauseSip(String customerId, Long sipMandateId);
    String resumeSip(String customerId, Long sipMandateId);
    String cancelSip(String customerId, Long sipMandateId);
}
