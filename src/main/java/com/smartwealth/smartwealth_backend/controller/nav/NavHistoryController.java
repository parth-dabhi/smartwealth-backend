package com.smartwealth.smartwealth_backend.controller.nav;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.nav.NavHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.nav.PlanNavResponse;
import com.smartwealth.smartwealth_backend.service.nav.NavHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.API_NAV_HISTORY)
@RequiredArgsConstructor
@Slf4j
public class NavHistoryController {

    private final NavHistoryService navHistoryService;

    @GetMapping
    public ResponseEntity<NavHistoryResponse> getNavHistory(@RequestParam Integer planId) {
        log.debug("Fetching NAV history for planId={}", planId);
        NavHistoryResponse response = navHistoryService.getNavHistory(planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiPaths.NAVS_BY_DATE)
    public List<PlanNavResponse> getPlanNavsByDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return navHistoryService.findPlanNavsByDate(date);
    }
}
