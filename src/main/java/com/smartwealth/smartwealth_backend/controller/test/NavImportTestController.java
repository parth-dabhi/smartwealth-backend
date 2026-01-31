package com.smartwealth.smartwealth_backend.controller.test;

import com.smartwealth.smartwealth_backend.service.nav.NavImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/nav")
public class NavImportTestController {

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
