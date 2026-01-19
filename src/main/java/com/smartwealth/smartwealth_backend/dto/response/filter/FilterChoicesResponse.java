package com.smartwealth.smartwealth_backend.dto.response.filter;

import com.smartwealth.smartwealth_backend.dto.common.FilterOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class FilterChoicesResponse {
    private List<FilterOption> amcs;
    private List<FilterOption> assets;
    private Map<String, List<FilterOption>> categories;
    private List<FilterOption> optionTypes;
}
