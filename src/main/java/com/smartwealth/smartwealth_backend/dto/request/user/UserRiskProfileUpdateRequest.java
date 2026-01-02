package com.smartwealth.smartwealth_backend.dto.request.user;

import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRiskProfileUpdateRequest {

    @NotNull(message = "riskProfile is required")
    private RiskProfile riskProfile;
}
