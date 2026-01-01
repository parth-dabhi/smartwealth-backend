package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.response.AdminUserDetailResponse;
import com.smartwealth.smartwealth_backend.dto.response.AdminUserListResponse;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;

public interface AdminUserService {
    /**
     * Returns a paginated, minimal list of users for admin navigation.
     *
     * @param customerId optional exact match
     * @param fullName optional partial match (case-insensitive)
     * @param kycStatus optional exact match
     * @param role optional exact match
     * @param isActive optional exact match
     * @param page page number (0-based)
     * @param size page size
     */
    AdminUserListResponse listUsers(
            String customerId,
            String fullName,
            KycStatus kycStatus,
            UserRole role,
            Boolean isActive,
            int page,
            int size
    );

    AdminUserDetailResponse getUserDetail(String customerId);

    void updateKycStatus(String customerId, KycStatus newStatus, String remark);
}
