package com.smartwealth.smartwealth_backend.repository.user.projection;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;

public interface UserEligibilityProjection {
    boolean getIsActive();
    KycStatus getKycStatus();
}
