package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.holding.HoldingTransaction;
import com.smartwealth.smartwealth_backend.repository.holding.HoldingTransactionRepository;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class HoldingTransactionServiceImpl implements HoldingTransactionService {

    private final HoldingTransactionRepository holdingTransactionRepository;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Override
    public void createHoldingTransactionRecord(
            Long holdingId,
            Long investmentOrderId,
            BigDecimal units,
            BigDecimal amount,
            HoldingTxnType txnType,
            InvestmentMode investmentMode,
            PlanNavProjection nav
    ) {
        HoldingTransaction holdingTransaction = HoldingTransaction.builder()
                .holdingId(holdingId)
                .investmentOrderId(investmentOrderId)
                .txnType(txnType)
                .investmentMode(investmentMode)
                .units(units)
                .amount(amount)
                .navDate(nav.getNavDate())
                .nav(nav.getNavValue())
                .transactionDate(OffsetDateTime.now(IST).toLocalDate())
                .createdAt(OffsetDateTime.now(IST))
                .build();

        holdingTransactionRepository.save(holdingTransaction);
        log.info("Holding transaction record created. holdingId={}, orderId={}, txnType={}, units={}",
                holdingId, investmentOrderId, txnType, units);
    }
}
