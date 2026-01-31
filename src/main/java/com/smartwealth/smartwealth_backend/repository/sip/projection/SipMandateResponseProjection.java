package com.smartwealth.smartwealth_backend.repository.sip.projection;

import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface SipMandateResponseProjection {

    Long getSipMandateId();
    Integer getPlanId();

    SipStatus getStatus();

    BigDecimal getSipAmount();
    Integer getSipDay();

    Integer getTotalInstallments();
    Integer getCompletedInstallments();

    LocalDate getStartDate();
    LocalDate getEndDate();
    OffsetDateTime getNextRunAt();
}

