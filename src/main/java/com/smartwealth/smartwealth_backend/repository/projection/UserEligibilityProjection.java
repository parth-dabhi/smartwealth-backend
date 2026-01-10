package com.smartwealth.smartwealth_backend.repository.projection;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;

public interface UserEligibilityProjection {
    Long getId();
    boolean getIsActive();
    KycStatus getKycStatus();
}
