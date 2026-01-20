package com.smartwealth.smartwealth_backend.dto.response.pagination;

import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
