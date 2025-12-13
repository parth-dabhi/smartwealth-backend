package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.mapper.UserMapper;
import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import com.smartwealth.smartwealth_backend.exception.ResourceAlreadyExistsException;
import com.smartwealth.smartwealth_backend.exception.ResourceNotFoundException;
import com.smartwealth.smartwealth_backend.repository.UserRepository;
import com.smartwealth.smartwealth_backend.service.CustomerIdGeneratorService;
import com.smartwealth.smartwealth_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CustomerIdGeneratorService customerIdGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            isolation = Isolation.READ_COMMITTED, // Prevents dirty reads, Other levels are overkill.
            label = "USER_CREATE_OPERATION"
    )
    public Optional<UserResponse> createUser(UserCreateRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        validateDuplicateUser(request);
        User user = UserMapper.toEntity(request);

        // manually set default values - double safety
        user.setRole(UserRole.USER);
        user.setKycStatus(KycStatus.PENDING);
        user.setRiskProfile(RiskProfile.MODERATE);
        // Hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Generate Customer ID
        user.setCustomerId(customerIdGenerator.generateCustomerId());
        // Save entity
        User savedUser  = userRepository.save(user);

        log.info("User created successfully. customerId={}", savedUser .getCustomerId());
        return Optional.of(UserMapper.toResponse(savedUser ));
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
    @Transactional(readOnly = true, label = "FETCH_USER_BY_ID")
    public Optional<UserResponse> getUserByCustomerId(String customerId) {
        log.info("Fetching user by customerId={}", customerId);
        User user = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found-Invalid Customer id"));
        return Optional.of(UserMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true, label = "FETCH_USER_BY_EMAIL")
    public Optional<UserResponse> getUserByEmail(String email) {
        log.info("Fetching user by email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return Optional.of(UserMapper.toResponse(user));
    }
}
