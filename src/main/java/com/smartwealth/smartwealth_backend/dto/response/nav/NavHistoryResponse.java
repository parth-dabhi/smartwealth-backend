package com.smartwealth.smartwealth_backend.dto.response.nav;

import com.smartwealth.smartwealth_backend.dto.common.NavHistoryPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NavHistoryResponse {

    private Integer planId;
    private List<NavHistoryPoint> navs;
}

