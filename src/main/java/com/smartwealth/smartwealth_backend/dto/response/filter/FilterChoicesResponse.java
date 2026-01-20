package com.smartwealth.smartwealth_backend.dto.response.filter;

import com.smartwealth.smartwealth_backend.dto.common.FilterOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterChoicesResponse {
    private List<FilterOption> amcs;
    private List<FilterOption> assets;
    private Map<String, List<FilterOption>> categories;
    private List<FilterOption> optionTypes;
}
