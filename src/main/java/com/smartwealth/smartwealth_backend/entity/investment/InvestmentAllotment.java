package com.smartwealth.smartwealth_backend.entity.investment;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "investment_allotment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_allotment_order",
                        columnNames = "investment_order_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class InvestmentAllotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_allotment_id")
    private Long investmentAllotmentId;

    @Column(name = "investment_order_id", nullable = false)
    private Long investmentOrderId;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(name = "nav_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal navValue;

    @Column(name = "units", nullable = false, precision = 18, scale = 8)
    private BigDecimal units;

    @Column(name = "allotted_at", nullable = false)
    private OffsetDateTime allottedAt;
}
