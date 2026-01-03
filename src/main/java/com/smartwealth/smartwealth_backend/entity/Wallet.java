package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallet_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ownership
    // One wallet belongs to exactly one user
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Available balance (can be spent)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // Reserved / locked funds (cannot be spent)
    @Column(name = "locked_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal lockedBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Wallet(User user) {
        this.user = user;
        this.balance = BigDecimal.ZERO;
        this.lockedBalance = BigDecimal.ZERO;
        this.status = WalletStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public static Wallet createFor(User user) {
        return new Wallet(user);
    }
}
