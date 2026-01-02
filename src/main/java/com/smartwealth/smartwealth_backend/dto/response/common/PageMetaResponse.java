package com.smartwealth.smartwealth_backend.dto.response.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageMetaResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
