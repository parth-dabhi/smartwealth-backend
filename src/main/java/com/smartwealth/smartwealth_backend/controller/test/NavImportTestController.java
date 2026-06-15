package com.smartwealth.smartwealth_backend.controller.test;

import com.smartwealth.smartwealth_backend.scheduler.NavAnchorScheduler;
import com.smartwealth.smartwealth_backend.service.nav.NavImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/nav")
public class NavImportTestController {

    private final NavImportService navImportService;
    private final NavAnchorScheduler navAnchorScheduler;

    @PostMapping("/import-today")
    public String fetchNavData() {
        return navImportService.importTodayNav();
    }

    @PostMapping("/import-historical")
    public String fetchHistoricalNavData(@RequestParam String date) {
        return navImportService.importHistoricalNav(date);
    }

    // TODO: NAV anchor refresh should ideally be tested in a separate test class, but for now we can trigger it here
    @PostMapping("/nav-anchors")
    public String refreshNavAnchors() {
        navAnchorScheduler.runNightlyPipeline();

        return "Nav anchors refreshed and returns/scores recalculated";
    }
}
