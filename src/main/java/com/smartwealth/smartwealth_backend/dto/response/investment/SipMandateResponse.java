package com.smartwealth.smartwealth_backend.dto.response.investment;

import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;
import com.smartwealth.smartwealth_backend.repository.sip.projection.SipMandateResponseProjection;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SipMandateResponse {
    private Long sipMandateId;
    private Integer planId;

    private SipStatus status;

    private BigDecimal sipAmount;
    private Integer sipDay;

    private Integer totalInstallments;
    private Integer completedInstallments;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate nextRunAt;

    public static SipMandateResponse fromProjection(SipMandateResponseProjection sip) {
        return SipMandateResponse.builder()
                .sipMandateId(sip.getSipMandateId())
                .planId(sip.getPlanId())
                .sipAmount(sip.getSipAmount())
                .sipDay(sip.getSipDay())
                .totalInstallments(sip.getTotalInstallments())
                .completedInstallments(sip.getCompletedInstallments())
                .startDate(sip.getStartDate())
                .endDate(sip.getEndDate())
                .status(sip.getStatus())
                .nextRunAt(sip.getNextRunAt() == null ? null : sip.getNextRunAt().toLocalDate())
                .build();
    }

    public static SipMandateResponse fromEntity(SipMandate sip) {
        return SipMandateResponse.builder()
                .sipMandateId(sip.getSipMandateId())
                .planId(sip.getPlanId())
                .sipAmount(sip.getSipAmount())
                .sipDay(sip.getSipDay())
                .totalInstallments(sip.getTotalInstallments())
                .completedInstallments(sip.getCompletedInstallments())
                .startDate(sip.getStartDate())
                .endDate(sip.getEndDate())
                .status(sip.getStatus())
                .nextRunAt(sip.getNextRunAt() == null ? null : sip.getNextRunAt().toLocalDate())
                .build();
    }
}
