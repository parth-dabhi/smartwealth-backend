package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.user.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.repository.projection.UserEligibilityProjection;

import java.util.Optional;

public interface UserService {

    Optional<UserAuthResponse> createUser(UserCreateRequest request);
    User getUserByCustomerId(String customerId);
    User getUserByEmail(String email);
    void updateRiskProfile(String customerId, RiskProfile riskProfile);
    Long getUserIdByCustomerId(String customerId);
    UserEligibilityProjection getUserEligibilityByCustomerId(String customerId);
}
