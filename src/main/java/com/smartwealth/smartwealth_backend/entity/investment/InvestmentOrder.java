package com.smartwealth.smartwealth_backend.entity.investment;

import com.smartwealth.smartwealth_backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "investment_order",
        indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_plan", columnList = "plan_id"),
                @Index(name = "idx_order_sip", columnList = "sip_mandate_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_nav_date", columnList = "applicable_nav_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Setter
public class InvestmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_order_id")
    private Long investmentOrderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "sip_mandate_id")
    private Long sipMandateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false, length = 20)
    private InvestmentType investmentType; // BUY, SELL

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_mode", nullable = false, length = 20)
    private InvestmentMode investmentMode; // LUMPSUM / SIP

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "units", precision = 18, scale = 6)
    private BigDecimal units; // required for Sell

    @Column(name = "order_time", nullable = false)
    private OffsetDateTime orderTime;

    @Column(name = "applicable_nav_date", nullable = false)
    private LocalDate applicableNavDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "payment_reference_id", length = 100, nullable = false)
    private String paymentReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason_type", length = 100)
    private FailureReasonType failureReasonType;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
