package com.smartwealth.smartwealth_backend.controller.sip;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.request.investment.CreateSipMandateRequest;
import com.smartwealth.smartwealth_backend.dto.response.investment.ListAllSipMandateResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.SipMandateResponse;
import com.smartwealth.smartwealth_backend.service.sip.SipMandateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.API_SIP)
@RequiredArgsConstructor
public class SipMandateController {

    private final SipMandateService sipMandateService;

    @PostMapping(ApiPaths.SIP_CREATE)
    public SipMandateResponse createSip(
            @Valid @RequestBody CreateSipMandateRequest request,
            @AuthenticationPrincipal String customerId
    ) {
        return sipMandateService.createSip(customerId, request);
    }

    @GetMapping(ApiPaths.GET_ALL_SIPS)
    public ResponseEntity<ListAllSipMandateResponse> getMySips(
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(sipMandateService.getAllUserSips(customerId));
    }

    @PostMapping(ApiPaths.SIP_PAUSE)
    public ResponseEntity<String> pauseSip(
            @RequestParam Long sipMandateId,
            @AuthenticationPrincipal String customerId
    ) {
        String message = sipMandateService.pauseSip(customerId, sipMandateId);
        return ResponseEntity.ok(message);
    }

    @PostMapping(ApiPaths.SIP_RESUME)
    public ResponseEntity<String> resumeSip(
            @RequestParam Long sipMandateId,
            @AuthenticationPrincipal String customerId
    ) {
        String message = sipMandateService.resumeSip(customerId, sipMandateId);
        return ResponseEntity.ok(message);
    }

    @PostMapping(ApiPaths.SIP_CANCEL)
    public ResponseEntity<String> cancelSip(
            @RequestParam Long sipMandateId,
            @AuthenticationPrincipal String customerId
    ) {
        String message = sipMandateService.cancelSip(customerId, sipMandateId);
        return ResponseEntity.ok(message);
    }
}
