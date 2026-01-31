package com.smartwealth.smartwealth_backend.controller.wallet;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.request.transaction.TransactionFilterRequest;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.WalletTransactionHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.API_WALLET)
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    /**
     * Get current wallet balance (READ-ONLY)
     */
    @GetMapping(ApiPaths.WALLET_BALANCE)
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @AuthenticationPrincipal String customerId
    ) {
        WalletBalanceResponse balance = walletService.getWalletBalance(customerId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Credit wallet
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(ApiPaths.WALLET_CREDIT)
    public ResponseEntity<TransactionResponse> creditWallet(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal String customerId
    ) {
        TransactionResponse transactionResponse = walletService.creditWallet(customerId, amount, idempotencyKey);
        return ResponseEntity.ok(transactionResponse);
    }

    /**
     * Debit wallet
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(ApiPaths.WALLET_DEBIT)
    public ResponseEntity<TransactionResponse> debitWallet(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal String customerId
    ) {
        TransactionResponse transactionResponse = walletService.debitWallet(customerId, amount, idempotencyKey);
        return ResponseEntity.ok(transactionResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(ApiPaths.WALLET_TRANSACTIONS)
    public ResponseEntity<PaginationResponse<WalletTransactionHistoryResponse>> getWalletTransactions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionCategory category,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal String customerId
    ) {
        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setType(type);
        filter.setCategory(category);
        filter.setStatus(status);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);

        return ResponseEntity.ok(
                walletService.getWalletTransactions(customerId, page, size, sortBy, sortDirection, filter)
        );
    }
}
