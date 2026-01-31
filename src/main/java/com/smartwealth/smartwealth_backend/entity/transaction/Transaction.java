package com.smartwealth.smartwealth_backend.entity.transaction;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.transaction.InvalidTransactionStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_user_id", columnList = "user_id"),
                @Index(name = "idx_tx_wallet_id", columnList = "wallet_id"),
                @Index(name = "idx_tx_category", columnList = "transaction_category")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of the transaction
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    // Wallet involved
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10, updatable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_category", nullable = false, length = 20, updatable = false)
    private TransactionCategory transactionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10, updatable = true)
    private TransactionStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "reference_id", nullable = false, length = 100, updatable = false, unique = true)
    private String referenceId;

    @Column(name = "description", updatable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Transaction(TransactionCreateCommand cmd, TransactionStatus status, BigDecimal currentBalance) {
        this.userId = cmd.getUserId();
        this.walletId = cmd.getWalletId();
        this.amount = cmd.getAmount();
        this.balanceBefore = currentBalance;
        this.balanceAfter = currentBalance;
        this.transactionType = cmd.getTransactionType();
        this.transactionCategory = cmd.getTransactionCategory();
        this.status = status;
        this.idempotencyKey = cmd.getIdempotencyKey();
        this.referenceId = cmd.getReferenceId();
        this.description = cmd.getDescription();
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Creates a PENDING transaction.
     * Used BEFORE wallet mutation.
     */
    public static Transaction createPending(TransactionCreateCommand command, BigDecimal currentBalance) {
        Objects.requireNonNull(command, "TransactionCreateCommand must not be null");
        return new Transaction(command, TransactionStatus.PENDING, currentBalance);
    }

    /**
     * Marks transaction as SUCCESS.
     * Called ONLY after wallet mutation succeeds.
     */
    public void markSuccess() {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Only PENDING transactions can be marked SUCCESS");
        }
        this.status = TransactionStatus.SUCCESS;
    }

    /**
     * Optional (future-proofing)
     */
    public void markFailed() {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Only PENDING transactions can be marked FAILED");
        }
        this.status = TransactionStatus.FAILED;
    }
}
