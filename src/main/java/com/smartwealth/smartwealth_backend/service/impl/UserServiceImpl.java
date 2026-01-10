package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.request.user.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.Wallet;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import com.smartwealth.smartwealth_backend.exception.resource.ResourceAlreadyExistsException;
import com.smartwealth.smartwealth_backend.exception.resource.ResourceNotFoundException;
import com.smartwealth.smartwealth_backend.repository.UserRepository;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.repository.projection.UserEligibilityProjection;
import com.smartwealth.smartwealth_backend.service.CustomerIdGeneratorService;
import com.smartwealth.smartwealth_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CustomerIdGeneratorService customerIdGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            isolation = Isolation.READ_COMMITTED, // Prevents dirty reads, Other levels are overkill.
            label = "USER_CREATE_OPERATION"
    )
    public Optional<UserAuthResponse> createUser(UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        validateDuplicateUser(request);
        User user = UserCreateRequest.toEntity(request);

        // manually set default values - double safety
        user.setRole(UserRole.USER);
        user.setKycStatus(KycStatus.PENDING);
        user.setRiskProfile(RiskProfile.MODERATE);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCustomerId(customerIdGenerator.generateCustomerId()); // Generate Customer ID - db call

        try {
            User savedUser  = userRepository.save(user);
            log.info("User created successfully. customerId={}", savedUser .getCustomerId());

            // Create Wallet for New User
            log.info("Creating wallet for user customerId={}", savedUser.getCustomerId());
            Wallet wallet = Wallet.createFor(savedUser.getId());
            walletRepository.save(wallet);
            log.info("Wallet created for userId={} walletId={}", savedUser.getId(), wallet.getId());

            return Optional.of(UserAuthResponse.toResponse(savedUser, null));
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("User with provided details already exists.");
        }
    }

    private void validateDuplicateUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered.");
        }
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new ResourceAlreadyExistsException("Mobile number already registered.");
        }
    }

    @Override
    public User getUserByCustomerId(String customerId) {
        log.info("Fetching user by customerId={}", customerId);
        return userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found-Invalid Customer id"));
    }

    @Override
    public User getUserByEmail(String email) {
        log.info("Fetching user by email={}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(timeout = 5)
    public void updateRiskProfile(String customerId, RiskProfile riskProfile) {

        log.info("Updating risk profile for customerId={}, newRiskProfile={}", customerId, riskProfile);

        User user = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Idempotent-safe
        if (user.getRiskProfile() == riskProfile) {
            log.info("Risk profile already set to {} for customerId={}", riskProfile, customerId);
            return;
        }

        user.setRiskProfile(riskProfile);
        userRepository.save(user);

        log.info("Risk profile updated successfully for customerId={}", customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserIdByCustomerId(String customerId) {
        log.info("Fetching user ID by customerId={}", customerId);
        return userRepository.findUserIdByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEligibilityProjection getUserEligibilityByCustomerId(String customerId) {
        log.info("Fetching user eligibility by customerId={}", customerId);
        return userRepository.findEligibilityByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
