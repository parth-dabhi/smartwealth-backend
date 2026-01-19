package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.filter.FilterChoicesResponse;
import com.smartwealth.smartwealth_backend.service.impl.FilterChoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_FILTERS)
@RequiredArgsConstructor
@Slf4j
public class FilterController {

    private final FilterChoiceService filterChoiceService;

    @GetMapping
    public ResponseEntity<FilterChoicesResponse> getFilterChoices() {
        log.debug("Fetching filter choices");
        FilterChoicesResponse response = filterChoiceService.getFilterChoices();
        return ResponseEntity.ok(response);
    }
}
