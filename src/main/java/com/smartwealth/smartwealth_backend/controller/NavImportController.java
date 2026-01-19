package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.service.impl.NavImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/nav")
public class NavImportController {

    private final NavImportService navImportService;

    @PostMapping("/import-today")
    public String fetchNavData() {
        return navImportService.importTodayNav();
    }

    @PostMapping("/import-historical")
    public String fetchHistoricalNavData(@RequestParam String date) {
        return navImportService.importHistoricalNav(date);
    }
}
