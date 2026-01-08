package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

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
            Principal principal
    ) {
        String customerId = principal.getName();
        WalletBalanceResponse balance = walletService.getWalletBalance(customerId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Credit wallet
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(ApiPaths.WALLET_CREDIT)
    public ResponseEntity<WalletBalanceResponse> creditWallet(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Principal principal,
            @RequestParam BigDecimal amount
    ) {
        String customerId = principal.getName();
        WalletBalanceResponse balanceResponse = walletService.creditWallet(customerId, amount, idempotencyKey);
        return ResponseEntity.ok(balanceResponse);
    }

    /**
     * Debit wallet
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(ApiPaths.WALLET_DEBIT)
    public ResponseEntity<WalletBalanceResponse> debitWallet(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Principal principal,
            @RequestParam BigDecimal amount
    ) {
        String customerId = principal.getName();
        WalletBalanceResponse balanceResponse = walletService.debitWallet(customerId, amount, idempotencyKey);
        return ResponseEntity.ok(balanceResponse);
    }
}
