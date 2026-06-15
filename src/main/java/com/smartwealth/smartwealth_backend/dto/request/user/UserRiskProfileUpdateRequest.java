package com.smartwealth.smartwealth_backend.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRiskProfileUpdateRequest {

    @NotNull(message = "riskProfile id is required")
    private Integer riskProfileId;
}
