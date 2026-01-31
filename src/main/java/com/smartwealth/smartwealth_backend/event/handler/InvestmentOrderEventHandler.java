package com.smartwealth.smartwealth_backend.event.handler;

import com.smartwealth.smartwealth_backend.entity.enums.FailureReasonType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import com.smartwealth.smartwealth_backend.event.InvestmentOrderFailedEvent;
import com.smartwealth.smartwealth_backend.event.SipExecutionFailedEvent;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.*;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionNotFoundException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletTransactionException;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvestmentOrderEventHandler {

    private final InvestmentOrderRepository investmentOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    // this event is also called after rollback of the main transaction and wallet releases
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleInvestmentOrderFailedEvent(InvestmentOrderFailedEvent event) {
        Long orderId = event.getOrderId();
        log.info("Handling InvestmentOrderCreatedEvent for orderId: {}", orderId);

        FailureReasonType failureReasonType = getFailureReasonType(event.getEx());

        int rowsUpdated = investmentOrderRepository.markOrderAsFailed(
                event.getOrderId(),
                OrderStatus.FAILED,
                event.getPaymentStatus(),
                event.getReferenceId() != null ? event.getReferenceId() : "N/A",
                failureReasonType,
                event.getEx().getMessage(),
                event.getFailedAt(),
                event.getFailedAt()
        );

        if (rowsUpdated == 0) {
            log.error("Failed to mark investment order as FAILED. orderId={}", orderId);
            throw new InvestmentOrderFailedException("Failed to mark investment order as FAILED");
        }

        if (event.getInvestmentMode().equals(InvestmentMode.SIP) && event.getEx() instanceof WalletTransactionException walletEx) {
                // mark SIP order as failed
                eventPublisher.publishEvent(
                        new SipExecutionFailedEvent(
                                event.getSipMandateId(),
                                event.getUserId(),
                                OffsetDateTime.now(),
                                walletEx
                        )
                );
        }

        log.info("Investment order marked as FAILED. orderId={}", orderId);
    }

    private static FailureReasonType getFailureReasonType(Exception ex) {
        return switch (ex) {
            case WalletTransactionException e -> FailureReasonType.WALLET_TRANSACTION_FAILURE;
            case TransactionFailedException e -> FailureReasonType.TRANSACTION_FAILURE;
            case BusinessLogicException e -> FailureReasonType.BUSINESS_LOGIC;
            case TemporarySystemException e -> FailureReasonType.TEMPORARY_SYSTEM_ISSUE;
            case IllegalArgumentException e -> FailureReasonType.INVALID_REQUEST;
            case IllegalStateException e -> FailureReasonType.ILLEGAL_STATE;
            case HoldingNotFoundException e -> FailureReasonType.HOLDING_NOT_FOUND;
            case HoldingUpdateFailedException e -> FailureReasonType.HOLDING_UPDATE_FAILED;
            case InvalidSellRequestException e -> FailureReasonType.INVALID_SELL_REQUEST;
            case PlanNotFoundException e -> FailureReasonType.PLAN_NOT_FOUND;
            case InvalidInvestmentOrderException e -> FailureReasonType.INVALID_INVESTMENT_ORDER;
            case TransactionNotFoundException e -> FailureReasonType.TRANSACTION_NOT_FOUND;
            default -> FailureReasonType.UNKNOWN;
        };
    }
}
