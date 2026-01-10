package com.smartwealth.smartwealth_backend.repository.projection;

import com.smartwealth.smartwealth_backend.entity.enums.WalletStatus;
import java.math.BigDecimal;

public interface WalletProjection {
    Long getId();
    BigDecimal getBalance();
    BigDecimal getLockedBalance();
    WalletStatus getStatus();
}
