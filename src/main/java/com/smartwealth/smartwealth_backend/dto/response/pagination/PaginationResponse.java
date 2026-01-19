package com.smartwealth.smartwealth_backend.dto.response.pagination;

import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import com.smartwealth.smartwealth_backend.repository.projection.SchemeProjection;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PaginationResponse<T> {
    private PageMetaResponse meta;
    private List<T> data;

//    public static <T> PaginationResponse<T> from(SchemeProjection schemesPage, List<T> data) {
//        return PaginationResponse.<T>builder()
//                .meta(PageMetaResponse.from((Page<?>) schemesPage))
//                .data(data)
//                .build();
//    }
}
