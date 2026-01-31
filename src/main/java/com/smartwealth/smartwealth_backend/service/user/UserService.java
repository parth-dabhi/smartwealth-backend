package com.smartwealth.smartwealth_backend.service.user;

import com.smartwealth.smartwealth_backend.dto.request.user.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.user.User;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.repository.user.projection.UserEligibilityProjection;

import java.util.Optional;

public interface UserService {

    UserAuthResponse createUser(UserCreateRequest request);
    User getUserByCustomerId(String customerId);
    User getUserByEmail(String email);
    void updateRiskProfile(String customerId, RiskProfile riskProfile);
    Long getUserIdByCustomerId(String customerId);
    UserEligibilityProjection getUserEligibilityByCustomerId(Long userId);
}
