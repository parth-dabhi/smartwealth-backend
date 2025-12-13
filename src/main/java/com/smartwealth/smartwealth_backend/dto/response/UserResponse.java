package com.smartwealth.smartwealth_backend.dto.response;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String customerId;
    private String email;
    private String mobileNumber;
    private String fullName;

    private UserRole role; // role of the user
    private KycStatus kycStatus;
    private RiskProfile riskProfile;

    private boolean isActive;
}
