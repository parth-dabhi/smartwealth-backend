package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.scheme.SchemeWithPlansResponse;

public interface MutualFundSchemeService {
    PaginationResponse<SchemeWithPlansResponse> getSchemes(
            Integer amcId,
            Integer assetId,
            Integer categoryId,
            Integer optionTypeId,
            String search,
            int page,
            int size
    );
}
