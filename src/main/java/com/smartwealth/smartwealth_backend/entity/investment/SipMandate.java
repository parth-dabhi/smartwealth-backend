package com.smartwealth.smartwealth_backend.entity.investment;

import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "sip_mandate",
        indexes = {
                @Index(name = "idx_sip_mandate_user", columnList = "user_id"),
                @Index(name = "idx_sip_mandate_plan", columnList = "plan_id"),
                @Index(name = "idx_sip_mandate_next_run", columnList = "next_run_at"),
                @Index(name = "idx_sip_mandate_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SipMandate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sip_mandate_id")
    private Long sipMandateId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "sip_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal sipAmount;

    /**
     * Day of month (1–28)
     */
    @Column(name = "sip_day", nullable = false)
    private Integer sipDay;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(name = "completed_installments", nullable = false)
    private Integer completedInstallments;

    // Scheduler Tracking

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "last_run_at")
    private OffsetDateTime lastRunAt;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SipStatus status;

    // failure
    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;

    @Column(name = "last_failure_at")
    private OffsetDateTime lastFailureAt;

    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
