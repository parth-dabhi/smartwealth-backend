package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ownership
    // One wallet belongs to exactly one user
    @Column(name = "user_id", nullable = false, updatable = false, unique = true)
    private Long userId;

    // Available balance (can be spent)
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // Reserved / locked funds (cannot be spent) Simple Balance ≥ Locked Balance
    @Column(name = "locked_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal lockedBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WalletStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Wallet(Long userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
        this.lockedBalance = BigDecimal.ZERO;
        this.status = WalletStatus.ACTIVE;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static Wallet createFor(Long userId) {
        return new Wallet(userId);
    }
}
