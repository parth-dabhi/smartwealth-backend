package com.smartwealth.smartwealth_backend.dto.response.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class WalletBalanceResponse {

    private BigDecimal totalBalance;
    private BigDecimal lockedBalance;
    private BigDecimal netBalance;
    private String message;

    public static WalletBalanceResponse from(BigDecimal totalBalance, BigDecimal lockedBalance, String message) {
        return WalletBalanceResponse.builder()
                .totalBalance(totalBalance)
                .lockedBalance(lockedBalance)
                .netBalance(totalBalance.subtract(lockedBalance))
                .message(message)
                .build();
    }
}
