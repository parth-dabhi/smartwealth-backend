package com.smartwealth.smartwealth_backend.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AdminUserListItemResponse {

    // Jackson will Convert Java objects to JSON

    private String customerId;
    private String fullName;

    /**
     * HATEOAS links for navigation.
     * Example:
     * {
     *   "self": "/admin/users/10000012"
     * }
     */
    private Map<String, String> _links;
}
