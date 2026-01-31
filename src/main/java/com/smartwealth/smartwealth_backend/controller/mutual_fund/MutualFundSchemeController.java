package com.smartwealth.smartwealth_backend.controller.mutual_fund;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.scheme.SchemeWithPlansResponse;
import com.smartwealth.smartwealth_backend.service.mutual_fund.MutualFundSchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_SCHEMES)
@RequiredArgsConstructor
@Slf4j
public class MutualFundSchemeController {

    private final MutualFundSchemeService mutualFundSchemeService;

    @GetMapping
    public ResponseEntity<PaginationResponse<SchemeWithPlansResponse>> getSchemes(
            @RequestParam(required = false) Integer amcId,
            @RequestParam(required = false) Integer assetId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer optionTypeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000000") int size
    ) {
        log.info(
                "Received request to get schemes with filters " +
                        "- amcId: {}, assetId: {}, categoryId: {}, option_type: {}, search: {}, page: {}, size: {}",
                amcId, assetId, categoryId, optionTypeId, search, page, size
        );

        return ResponseEntity.ok(
                mutualFundSchemeService.getSchemes(amcId, assetId, categoryId, optionTypeId, search, page, size)
        );
    }
}