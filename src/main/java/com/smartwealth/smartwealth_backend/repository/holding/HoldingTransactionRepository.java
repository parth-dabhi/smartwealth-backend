package com.smartwealth.smartwealth_backend.repository.holding;

import com.smartwealth.smartwealth_backend.entity.holding.HoldingTransaction;
import com.smartwealth.smartwealth_backend.repository.holding.projection.HoldingTxnProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoldingTransactionRepository extends JpaRepository<HoldingTransaction, Long> {
    @Query("""
        SELECT
            ht.txnType as txnType,
            ht.investmentMode as investmentMode,
            ht.units as units,
            ht.amount as amount,
            ht.nav as nav,
            ht.navDate as navDate,
            ht.transactionDate as transactionDate
        FROM HoldingTransaction ht
        WHERE ht.holdingId = :holdingId
        ORDER BY ht.transactionDate DESC
    """)
    List<HoldingTxnProjection> findByHoldingId(Long holdingId);
}
