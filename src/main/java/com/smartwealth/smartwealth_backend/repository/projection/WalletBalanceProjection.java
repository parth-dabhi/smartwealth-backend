package com.smartwealth.smartwealth_backend.repository.projection;

import java.math.BigDecimal;

public interface  WalletBalanceProjection {
    BigDecimal getBalance();
    BigDecimal getLockedBalance();
}
