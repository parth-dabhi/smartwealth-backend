package com.smartwealth.smartwealth_backend.repository.specification;

import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;

public final class AdminUserSpecification {
    private AdminUserSpecification() {
        // utility class
    }

    public static Specification<User> hasCustomerId(String customerId) {
        return (root, query, cb) ->
                customerId == null ? null : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<User> hasFullNameLike(String fullName) {
        return (root, query, cb) ->
                fullName == null ? null :
                        cb.like(
                                cb.lower(root.get("fullName")),
                                "%" + fullName.toLowerCase() + "%"
                        );
    }

    public static Specification<User> hasKycStatus(KycStatus kycStatus) {
        return (root, query, cb) ->
                kycStatus == null ? null : cb.equal(root.get("kycStatus"), kycStatus);
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<User> isActive(Boolean isActive) {
        return (root, query, cb) ->
                isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }
}
