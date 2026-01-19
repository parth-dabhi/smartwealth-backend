package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class CategoryFilterOption {
    private String group;   // Equity / Debt / Hybrid / Commodities
    private String label;   // category_short_name
    private Integer value;  // category_id
}
