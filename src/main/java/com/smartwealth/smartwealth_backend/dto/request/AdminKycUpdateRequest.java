package com.smartwealth.smartwealth_backend.dto.request;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AdminKycUpdateRequest {

    @NotNull(message = "kycStatus is required")
    private KycStatus kycStatus;

    // Optional admin remark (for audit / future)
    private String remark;
}
