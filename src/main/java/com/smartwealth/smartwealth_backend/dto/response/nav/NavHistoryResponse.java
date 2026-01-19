package com.smartwealth.smartwealth_backend.dto.response.nav;

import com.smartwealth.smartwealth_backend.dto.common.NavHistoryPoint;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NavHistoryResponse {

    private Integer planId;
    private List<NavHistoryPoint> navs;
}

