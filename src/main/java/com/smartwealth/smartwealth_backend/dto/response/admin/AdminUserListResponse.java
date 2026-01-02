package com.smartwealth.smartwealth_backend.dto.response.admin;

import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AdminUserListResponse {

    private PageMetaResponse meta;
    private List<AdminUserListItemResponse> data;

    /**
     * Collection-level HATEOAS links (self, next, prev)
     */
    private Map<String, String> _links;
}
