package com.smartwealth.smartwealth_backend.entity.holding;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "holding_transactions",
        indexes = {
                @Index(name = "idx_holding_txn_holding", columnList = "holding_id"),
                @Index(name = "idx_holding_txn_order", columnList = "investment_order_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HoldingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_txn_id")
    private Long holdingTxnId;

    @Column(name = "holding_id", nullable = false)
    private Long holdingId;

    @Column(name = "investment_order_id")
    private Long investmentOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 20)
    private HoldingTxnType txnType; // BUY | SELL

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_mode", nullable = false, length = 20)
    private InvestmentMode investmentMode;

    @Column(
            name = "units",
            nullable = false,
            precision = 18,
            scale = 8
    )
    private BigDecimal units;

    @Column(
            name = "amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "nav",
            precision = 12,
            scale = 4
    )
    private BigDecimal nav;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(name = "txn_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

