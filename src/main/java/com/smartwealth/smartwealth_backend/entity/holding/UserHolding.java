package com.smartwealth.smartwealth_backend.entity.holding;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Column(name = "folio_number", nullable = false, length = 20, unique = true, updatable = false)
    private String folioNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(
            name = "total_units",
            nullable = false,
            precision = 18,
            scale = 8
    )
    private BigDecimal totalUnits;

    @Column(
            name = "total_invested_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalInvestedAmount;

    @Column(
            name = "total_redeemed_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalRedeemedAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

