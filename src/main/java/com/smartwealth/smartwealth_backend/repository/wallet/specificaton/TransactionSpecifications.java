package com.smartwealth.smartwealth_backend.repository.wallet.specificaton;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
        // utility class
    }

    public static Specification<Transaction> hasWalletId(Long walletId) {
        return (root, query, cb) ->
                walletId == null ? null : cb.equal(root.get("walletId"), walletId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("transactionType"), type);
    }

    public static Specification<Transaction> hasCategory(TransactionCategory category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("transactionCategory"), category);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> createdAfter(OffsetDateTime startDate) {
        return (root, query, cb) ->
                startDate == null ? null :
                        cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<Transaction> createdBefore(OffsetDateTime endDate) {
        return (root, query, cb) ->
                endDate == null ? null :
                        cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }
}

