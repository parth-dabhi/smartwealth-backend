package com.smartwealth.smartwealth_backend.event;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class InvestmentOrderFailedEvent {

    private final Long orderId;
    private final Long userId;
    private final Long sipMandateId;
    private final OffsetDateTime failedAt;
    private final InvestmentMode investmentMode;
    private final PaymentStatus paymentStatus;
    private final String referenceId;
    private final Exception ex;
}
