package com.smartwealth.smartwealth_backend.entity.mutual_fund;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "nav_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nav_plan_date", columnNames = {"plan_id", "nav_date"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NavHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long navId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(name = "nav_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal navValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public NavHistory(Integer planId, LocalDate navDate, BigDecimal navValue) {
        this.planId = planId;
        this.navDate = navDate;
        this.navValue = navValue;
    }
}
