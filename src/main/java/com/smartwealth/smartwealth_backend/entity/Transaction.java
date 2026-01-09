package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_user_id", columnList = "user_id"),
                @Index(name = "idx_tx_wallet_id", columnList = "wallet_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tx_idempotency_key",
                        columnNames = "idempotency_key"
                ),
                @UniqueConstraint(
                        name = "uk_tx_reference_id",
                        columnNames = "reference_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of the transaction
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // Wallet involved
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10, updatable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = true)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "reference_id", nullable = false, length = 100, updatable = false)
    private String referenceId;

    @Column(length = 255, updatable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Transaction(TransactionCreateCommand cmd, TransactionStatus status) {
        this.user = cmd.getUser();
        this.wallet = cmd.getWallet();
        this.amount = cmd.getAmount();
        this.transactionType = cmd.getTransactionType();
        this.status = status;
        this.idempotencyKey = cmd.getIdempotencyKey();
        this.referenceId = cmd.getReferenceId();
        this.description = cmd.getDescription();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a PENDING transaction.
     * Used BEFORE wallet mutation.
     */
    public static Transaction createPending(TransactionCreateCommand command) {
        Objects.requireNonNull(command, "TransactionCreateCommand must not be null");
        return new Transaction(command, TransactionStatus.PENDING);
    }

    /**
     * Marks transaction as SUCCESS.
     * Called ONLY after wallet mutation succeeds.
     */
    public void markSuccess() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be marked SUCCESS");
        }
        this.status = TransactionStatus.SUCCESS;
    }

    /**
     * Optional (future-proofing)
     */
    public void markFailed() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be marked FAILED");
        }
        this.status = TransactionStatus.FAILED;
    }
}
