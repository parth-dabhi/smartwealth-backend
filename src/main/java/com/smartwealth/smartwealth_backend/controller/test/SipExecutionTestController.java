package com.smartwealth.smartwealth_backend.controller.test;

import com.smartwealth.smartwealth_backend.scheduler.SipExecutionScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/sip-execution")
@RequiredArgsConstructor
public class SipExecutionTestController {

    private final SipExecutionScheduler sipExecutionScheduler; // Temporary use of scheduler, working as a service

    @PostMapping("/execute")
    public String executeSipMandates() {
        return sipExecutionScheduler.executeSipMandates();
    }
}
