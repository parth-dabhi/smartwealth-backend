package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.dto.request.AdminKycUpdateRequest;
import com.smartwealth.smartwealth_backend.dto.response.AdminUserDetailResponse;
import com.smartwealth.smartwealth_backend.dto.response.AdminUserListResponse;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import com.smartwealth.smartwealth_backend.service.AdminUserService;
import com.smartwealth.smartwealth_backend.service.UserService;
import com.smartwealth.smartwealth_backend.api.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.API_ADMIN)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    /**
     * Mock KYC verification / update (Admin only).
     * Allowed transitions:
     * PENDING -> VERIFIED
     * PENDING -> REJECTED
     */

    private final AdminUserService adminUserService;

    @GetMapping(ApiPaths.USERS)
    public AdminUserListResponse listUsers(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) KycStatus kycStatus,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,

            @RequestParam(defaultValue = "0", name = "page") @Min(0) int page,
            @RequestParam(defaultValue = "20", name = "size") @Min(1) @Max(100) int size
    ) {
        log.info("AdminController: list users request received");

        return adminUserService.listUsers(
                customerId,
                fullName,
                kycStatus,
                role,
                isActive,
                page,
                size
        );
    }

    @GetMapping(ApiPaths.USER_BY_ID)
    public AdminUserDetailResponse getUserDetail(@PathVariable String customerId) {
        log.info("AdminController: get user detail for customerId={}", customerId);
        return adminUserService.getUserDetail(customerId);
    }

    @PutMapping(ApiPaths.USER_KYC_UPDATE)
    public ResponseEntity<Void> updateKyc(@PathVariable String customerId, @Valid @RequestBody AdminKycUpdateRequest request) {
        log.info("AdminController: update KYC for customerId={}", customerId);

        adminUserService.updateKycStatus(customerId, request.getKycStatus(), request.getRemark());

        return ResponseEntity.noContent().build();
    }
}
