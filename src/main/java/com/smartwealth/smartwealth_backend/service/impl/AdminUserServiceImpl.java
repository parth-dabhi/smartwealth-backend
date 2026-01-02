package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.response.admin.AdminUserDetailResponse;
import com.smartwealth.smartwealth_backend.dto.response.admin.AdminUserListItemResponse;
import com.smartwealth.smartwealth_backend.dto.response.admin.AdminUserListResponse;
import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import com.smartwealth.smartwealth_backend.dto.response.user.AddressResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import com.smartwealth.smartwealth_backend.exception.KycTransitionException;
import com.smartwealth.smartwealth_backend.exception.ResourceNotFoundException;
import com.smartwealth.smartwealth_backend.repository.UserRepository;
import com.smartwealth.smartwealth_backend.repository.projection.AdminUserListProjection;
import com.smartwealth.smartwealth_backend.repository.specification.AdminUserSpecification;
import com.smartwealth.smartwealth_backend.service.AdminUserService;
import com.smartwealth.smartwealth_backend.api.ApiPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private static final String PAGED_ADMIN_USERS_URL = ApiPaths.API_ADMIN_USERS + "?page=%d&size=%d";

    @Override
    @Transactional(readOnly = true, timeout = 5)
    public AdminUserListResponse listUsers(
            String customerId,
            String fullName,
            KycStatus kycStatus,
            UserRole role,
            Boolean isActive,
            int page,
            int size
    ) {

        log.info(
                "Listing admin users with filters - customerId: {}, fullName: {}, kycStatus: {}, role: {}, isActive: {}, page: {}, size: {}",
                customerId, fullName, kycStatus, role, isActive, page, size
        );

        Pageable pageable = PageRequest.of(page, size);

        Specification<User> specification =
                Specification.allOf(
                        AdminUserSpecification.hasCustomerId(customerId),
                        AdminUserSpecification.hasFullNameLike(fullName),
                        AdminUserSpecification.hasKycStatus(kycStatus),
                        AdminUserSpecification.hasRole(role),
                        AdminUserSpecification.isActive(isActive)
                );

        // Important: Uses projection, specification, and pagination
        Page<AdminUserListProjection> resultPage = userRepository.findBy(specification, query -> query
                .as(AdminUserListProjection.class) // Specify the projection type
                .page(pageable)
        );

        /*
        Specification - decides WHICH ROWS
        Projection - decides WHICH COLUMNS
        Pageable -  decides HOW MANY + WHICH PAGE
        Spring Data combines all three automatically.
         */

        List<AdminUserListItemResponse> items =
                resultPage.getContent().stream()
                        .map(p ->
                                AdminUserListItemResponse.builder()
                                        .customerId(p.getCustomerId())
                                        .fullName(p.getFullName())
                                        ._links(Map.of(
                                                "self",
                                                ApiPaths.API_ADMIN_USERS + "/" + p.getCustomerId()
                                        ))
                                        .build()
                        )
                        .toList();

        PageMetaResponse meta =
                PageMetaResponse.builder()
                        .page(resultPage.getNumber())
                        .size(resultPage.getSize())
                        .totalElements(resultPage.getTotalElements())
                        .totalPages(resultPage.getTotalPages())
                        .build();

        Map<String, String> links = new HashMap<>();

        links.put("self", String.format(PAGED_ADMIN_USERS_URL, page, size));
        if (resultPage.hasNext()) {
            links.put("next", String.format(PAGED_ADMIN_USERS_URL, page + 1, size));
        }
        if (resultPage.hasPrevious()) {
            links.put("prev", String.format(PAGED_ADMIN_USERS_URL, page - 1, size));
        }

        log.info(
                "Admin list users response prepared [customerId={}, fullName={}, kycStatus={}, role={}, isActive={}, page={}, size={}, returnedItems={}]",
                customerId, fullName, kycStatus, role, isActive, page, size, items.size()
        );

        return AdminUserListResponse.builder()
                .meta(meta)
                .data(items)
                ._links(links)
                .build();
    }

    @Override
    @Transactional(readOnly = true, timeout = 5)
    public AdminUserDetailResponse getUserDetail(String customerId) {

        log.info("Admin requested user detail for customerId={}", customerId);

        User user = userRepository.findByCustomerId(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return AdminUserDetailResponse.builder()
                .customerId(user.getCustomerId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(
                        user.getAddress() == null ? null :
                                AddressResponse.builder()
                                        .addressLine1(user.getAddress().getAddressLine1())
                                        .addressLine2(user.getAddress().getAddressLine2())
                                        .city(user.getAddress().getCity())
                                        .state(user.getAddress().getState())
                                        .country(user.getAddress().getCountry())
                                        .postalCode(user.getAddress().getPostalCode())
                                        .build()
                )
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                ._links(Map.of(
                        "self", ApiPaths.API_ADMIN_USERS  + "/" + customerId,
                        "list", ApiPaths.API_ADMIN_USERS
                ))
                .build();
    }

    @Override
    @Transactional(timeout = 5)
    public void updateKycStatus(String customerId, KycStatus newStatus, String remark) {

        log.info("Admin KYC update requested customerId={}, newStatus={}", customerId, newStatus);

        User user = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        KycStatus current = user.getKycStatus();

        // Idempotent-safe
        if (current == newStatus) {
            log.info("KYC already in status {} for customerId={}", current, customerId);
            return;
        }

        // Transition validation
        if (!isValidTransition(current, newStatus)) {
            throw new KycTransitionException(
                    "Invalid KYC transition: " + current + " → " + newStatus
            );
        }

        user.setKycStatus(newStatus);
        userRepository.save(user);

        log.info(
                "KYC updated successfully customerId={}, from {} to {}, remark={}",
                customerId, current, newStatus, remark
        );
    }

    /*
    Allowed transitions:
        PENDING → VERIFIED
        PENDING → REJECTED
        REJECTED → VERIFIED

     Not allowed:
        VERIFIED → ANY
     */

    private boolean isValidTransition(KycStatus current, KycStatus next) {
        return switch (current) {
            case PENDING -> next == KycStatus.VERIFIED || next == KycStatus.REJECTED;
            case REJECTED -> next == KycStatus.VERIFIED;
            case VERIFIED -> false;
        };
    }
}