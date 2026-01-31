package com.smartwealth.smartwealth_backend.service.wallet;

import com.smartwealth.smartwealth_backend.dto.request.transaction.TransactionFilterRequest;
import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.WalletTransactionHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.entity.enums.WalletStatus;
import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import com.smartwealth.smartwealth_backend.entity.transaction.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.exception.user.InactiveAccountException;
import com.smartwealth.smartwealth_backend.exception.user.KycVerificationException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletSuspendedException;
import com.smartwealth.smartwealth_backend.repository.user.projection.UserEligibilityProjection;
import com.smartwealth.smartwealth_backend.repository.wallet.projection.WalletProjection;
import com.smartwealth.smartwealth_backend.repository.wallet.projection.WalletTransactionHistoryProjection;
import com.smartwealth.smartwealth_backend.repository.wallet.specificaton.TransactionSpecifications;
import com.smartwealth.smartwealth_backend.repository.wallet.TransactionRepository;
import com.smartwealth.smartwealth_backend.repository.wallet.WalletRepository;
import com.smartwealth.smartwealth_backend.service.common.AfterCommitRedisService;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserService userService;
    private final WalletRepository walletRepository;
    private final WalletTransactionExecutor walletTransactionExecutor;
    private final AfterCommitRedisService afterCommitRedisService;
    private final TransactionRepository transactionRepository;

    private static final String KEY_PREFIX_IDEMPOTENCY = "tx:idempotency:";

    // Balance Retrieval

    @Override
    @Transactional(readOnly = true, label = "WALLET_BALANCE_RETRIEVAL_OPERATION")
    public WalletBalanceResponse getWalletBalance(String customerId) {
        Long userId = getUserId(customerId);

        WalletProjection projection = getWalletProjectionByUserId(userId);
        if (projection.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletSuspendedException(
                    "Wallet is not active for customerId=" + customerId
            );
        }

        return WalletBalanceResponse.from(
                projection.getBalance(),
                projection.getLockedBalance(),
                "Wallet balance retrieved successfully"
        );
    }

    // CREDIT

    @Override
    public TransactionResponse creditWallet(
            String customerId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        Long userId = getUserId(customerId);
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.TOP_UP,
                TransactionType.CREDIT,
                walletTransactionExecutor::creditWalletExecutor // (command) -> walletTransactionExecutor.creditWalletExecutor(command)
        );
    }

    @Override
    public TransactionResponse creditWallet(
            Long userId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.REDEMPTION,
                TransactionType.CREDIT,
                walletTransactionExecutor::creditWalletExecutor
        );
    }

    // DEBIT

    @Override
    public TransactionResponse debitWallet(
            String customerId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        Long userId = getUserId(customerId);
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.WITHDRAWAL,
                TransactionType.DEBIT,
                walletTransactionExecutor::debitWalletExecutor
        );
    }

    // LOCK / UNLOCK / DEBIT LOCKED

    @Override
    public TransactionResponse lockAmountInWallet(
            Long userId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.INVESTMENT,
                TransactionType.LOCK,
                walletTransactionExecutor::lockAmountInWalletExecutor
        );
    }

    @Override
    public TransactionResponse unlockAmountInWallet(
            Long userId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.REFUND,
                TransactionType.UNLOCK,
                walletTransactionExecutor::unlockAmountInWalletExecutor
        );
    }

    @Override
    public TransactionResponse debitLockedAmountInWallet(
            Long userId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.INVESTMENT,
                TransactionType.DBT_LOCKED,
                walletTransactionExecutor::debitLockedAmountInWalletExecutor
        );
    }

    @Override
    public TransactionResponse refundDebitedAmountInWallet(
            Long userId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return executeWalletOperation(
                userId,
                amount,
                idempotencyKey,
                TransactionCategory.REFUND,
                TransactionType.CREDIT,
                walletTransactionExecutor::refundDebitedAmountInWalletExecutor
        );
    }

    @Override
    @Transactional(readOnly = true, label = "WALLET_TRANSACTION_HISTORY_RETRIEVAL_OPERATION")
    public PaginationResponse<WalletTransactionHistoryResponse> getWalletTransactions(
            String customerId,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            TransactionFilterRequest filter
    ) {
        Long userId = getUserId(customerId);

        // Validate wallet exists and is accessible
        WalletProjection wallet = getWalletProjectionByUserId(userId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                buildSort(sortBy, sortDirection)
        );

        Specification<Transaction> specification = Specification.allOf(
                TransactionSpecifications.hasWalletId(wallet.getId()),
                TransactionSpecifications.hasType(filter.getType()),
                TransactionSpecifications.hasCategory(filter.getCategory()),
                TransactionSpecifications.hasStatus(filter.getStatus()),
                TransactionSpecifications.createdAfter(
                        filter.getStartDate() != null
                                ? filter.getStartDate().atStartOfDay().atOffset(ZoneOffset.UTC)
                                : null
                ),
                TransactionSpecifications.createdBefore(
                        filter.getEndDate() != null
                                ? filter.getEndDate().atTime(23, 59, 59).atOffset(ZoneOffset.UTC)
                                : null)
        );

        // Fetch transactions with filters
        Page<WalletTransactionHistoryProjection> transactionPage = transactionRepository.findBy(
                specification,
                query -> query
                        .as(WalletTransactionHistoryProjection.class)
                        .page(pageable)
        );

        // Map to response
        List<WalletTransactionHistoryResponse> responses = transactionPage.getContent()
                .stream()
                .map(WalletTransactionHistoryResponse::fromProjection)
                .toList();

        return PaginationResponse.<WalletTransactionHistoryResponse>builder()
                .meta(PageMetaResponse.from(transactionPage))
                .data(responses)
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        String sortField = switch (sortBy.toLowerCase()) {
            case "amount" -> "amount";
            case "date" -> "createdAt";
            default -> "createdAt";
        };

        return Sort.by(direction, sortField);
    }

    // COMMON ORCHESTRATION

    private TransactionResponse executeWalletOperation(
            Long userId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionCategory category,
            TransactionType type,
            Function<TransactionCreateCommand, TransactionResponse> executor
    ) {
        // REDIS IDEMPOTENCY CHECK
        String redisKey = getRedisKey(idempotencyKey);

        TransactionResponse cachedResponse =
                afterCommitRedisService.redisIdempotencyCheck(redisKey, idempotencyKey, TransactionResponse.class);
        if (cachedResponse != null) {
            cachedResponse.setMessage(cachedResponse.getMessage() + " (from cache)");
            return cachedResponse;
        }

        TransactionCreateCommand command =
                getTransactionCreateCommand(
                        userId,
                        amount,
                        idempotencyKey,
                        category,
                        type
                );

        return executor.apply(command);
    }


    private TransactionCreateCommand getTransactionCreateCommand(
            Long userId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionCategory category,
            TransactionType type
    ) {
        validatePositiveAmount(amount);

        UserEligibilityProjection eligibility =
                userService.getUserEligibilityByCustomerId(userId);

        validateUserEligibility(eligibility.getIsActive(), eligibility.getKycStatus());

        WalletProjection wallet = getWalletProjectionByUserId(userId);

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletSuspendedException(
                    "Wallet is not active, can't perform " + category.name()
            );
        }

        return TransactionCreateCommand.from(
                userId,
                wallet.getId(),
                amount,
                idempotencyKey,
                type,
                category
        );
    }

    // VALIDATIONS

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be positive"
            );
        }
    }

    private void validateUserEligibility(
            boolean isActive,
            KycStatus kycStatus
    ) {
        if (!isActive) {
            throw new InactiveAccountException(
                    "User account is not active"
            );
        }
        if (kycStatus != KycStatus.VERIFIED) {
            throw new KycVerificationException(
                    "KYC verification is "
                            + kycStatus.name().toLowerCase()
                            + ". Transaction not allowed."
            );
        }
    }

    // HELPERS

    private WalletProjection getWalletProjectionByUserId(Long userId) {
        return walletRepository
                .findWalletProjectionByUserId(userId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found for userId=" + userId
                        )
                );
    }

    private Long getUserId(String customerId) {
        return userService.getUserIdByCustomerId(customerId);
    }

    private String getRedisKey(String idempotencyKey) {
        return KEY_PREFIX_IDEMPOTENCY + idempotencyKey;
    }
}
