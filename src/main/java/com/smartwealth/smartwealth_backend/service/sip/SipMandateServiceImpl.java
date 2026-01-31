package com.smartwealth.smartwealth_backend.service.sip;

import com.smartwealth.smartwealth_backend.dto.request.investment.CreateSipMandateRequest;
import com.smartwealth.smartwealth_backend.dto.response.investment.ListAllSipMandateResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.SipMandateResponse;
import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.*;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.PlanSipConfigProjection;
import com.smartwealth.smartwealth_backend.repository.sip.SipMandateRepository;
import com.smartwealth.smartwealth_backend.repository.sip.projection.SipResumeProjection;
import com.smartwealth.smartwealth_backend.repository.sip.projection.SipStatusProjection;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SipMandateServiceImpl implements SipMandateService {

    private final SipMandateRepository sipMandateRepository;
    private final UserService userService;
    private final SchemePlanRepository schemePlanRepository;

    @Override
    @Transactional
    public SipMandateResponse createSip(String customerId, CreateSipMandateRequest request) {
        Long userId = getUserId(customerId);
        log.info("Creating SIP mandate for userId: {}, request: {}", userId, request);
        validateCreateRequest(request);

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Kolkata"));

        // For, first time startDate and nextRunAt are the same.
        OffsetDateTime startDateTime = computeFirstRunAt(request.getSipDay());
        LocalDate endDate = calculateEndDate(startDateTime.toLocalDate(), request.getTotalInstallments());

        SipMandate sip = SipMandate.builder()
                .userId(userId)
                .planId(request.getPlanId())
                .sipAmount(request.getSipAmount())
                .sipDay(request.getSipDay())
                .startDate(startDateTime.toLocalDate())
                .endDate(endDate)
                .totalInstallments(request.getTotalInstallments())
                .completedInstallments(0)
                .status(SipStatus.ACTIVE)
                .failureCount(0)
                .lastFailureAt(null)
                .nextRunAt(startDateTime)
                .lastRunAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sipMandateRepository.save(sip);

        log.info("SIP mandate successfully created with id: {}", sip.getSipMandateId());

        return SipMandateResponse.fromEntity(sip);
    }

    @Override
    @Transactional(readOnly = true)
    public ListAllSipMandateResponse getAllUserSips(String customerId) {
        Long userId = getUserId(customerId);

        List<SipMandateResponse> sipMandates = sipMandateRepository.findAllSipMandatesByUserId(userId)
                        .stream()
                        .map(SipMandateResponse::fromProjection)
                        .toList();

        int totalSips = sipMandates.size();

        Map<SipStatus, Long> statusCountMap = sipMandates.stream()
                .collect(Collectors.groupingBy(
                        SipMandateResponse::getStatus,
                        Collectors.counting()
                ));

        log.info("Fetched {} SIP mandates for userId: {}", totalSips, userId);

        return ListAllSipMandateResponse.builder()
                .totalSips(totalSips)
                .activeSips(statusCountMap.getOrDefault(SipStatus.ACTIVE, 0L).intValue())
                .pausedSips(statusCountMap.getOrDefault(SipStatus.PAUSED, 0L).intValue())
                .cancelledSips(statusCountMap.getOrDefault(SipStatus.CANCELLED, 0L).intValue())
                .completedSips(statusCountMap.getOrDefault(SipStatus.COMPLETED, 0L).intValue())
                .suspendedSips(statusCountMap.getOrDefault(SipStatus.SUSPENDED, 0L).intValue())
                .sipMandates(sipMandates)
                .build();
    }

    @Override
    @Transactional
    public String pauseSip(String customerId, Long sipMandateId) {
        Long userId = getUserId(customerId);
        SipStatus sipStatus = sipMandateRepository.findBySipMandateIdAndUserId(sipMandateId, userId, SipStatusProjection.class)
                .orElseThrow(() -> new SipMandateNotFoundException("SIP mandate not found"))
                .getStatus();

        // Additional check: only ACTIVE SIPs can be paused
        if (!sipStatus.equals(SipStatus.ACTIVE)) {
            throw new InvalidSipStateException("Only ACTIVE SIP mandates can be paused");
        }

        int updatedRows = sipMandateRepository.updateSipStatusBySipMandateIdAndUserId(
                sipMandateId,
                userId,
                SipStatus.PAUSED,
                null,
                OffsetDateTime.now()
        ); // also sets nextRunAt to null because of pause

        if (updatedRows == 0) {
            throw new InvalidSipStateException("Failed to pause SIP mandate. Please try again.");
        }

        return "SIP mandate PAUSED successfully";
    }

    @Override
    @Transactional
    public String resumeSip(String customerId, Long sipMandateId) {
        Long userId = getUserId(customerId);
        SipResumeProjection sipResumeProjection = sipMandateRepository.findBySipMandateIdAndUserId(sipMandateId, userId, SipResumeProjection.class)
                .orElseThrow(() -> new SipMandateNotFoundException("SIP mandate not found"));

        SipStatus sipStatus = sipResumeProjection.getStatus();
        Integer sipDay = sipResumeProjection.getSipDay();

        // only PAUSED SIPs can be resumed
        if (!sipStatus.equals(SipStatus.PAUSED)) {
            throw new InvalidSipStateException("Only PAUSED SIP mandates can be resumed");
        }

        OffsetDateTime nextRunAt = computeFirstRunAt(sipDay);

        int updatedRows = sipMandateRepository.updateSipStatusBySipMandateIdAndUserId(
                sipMandateId,
                userId,
                SipStatus.ACTIVE,
                nextRunAt,
                OffsetDateTime.now()
        );

        if (updatedRows == 0) {
            throw new InvalidSipStateException("Failed to resume SIP mandate. Please try again.");
        }

        return "SIP mandate RESUMED successfully";
    }

    @Override
    @Transactional
    public String cancelSip(String customerId, Long sipMandateId) {
        Long userId = getUserId(customerId);
        SipStatus sipStatus = sipMandateRepository.findBySipMandateIdAndUserId(sipMandateId, userId, SipStatusProjection.class)
                .orElseThrow(() -> new SipMandateNotFoundException("SIP mandate not found"))
                .getStatus();

        // Additional check: only ACTIVE or PAUSED SIPs can be cancelled
        if (!(sipStatus.equals(SipStatus.ACTIVE) || sipStatus.equals(SipStatus.PAUSED))) {
            throw new InvalidSipStateException("Only ACTIVE or PAUSED SIP mandates can be cancelled");
        }

        // sip.setEndDate(now); BAKI
        int updatedRows = sipMandateRepository.updateSipStatusBySipMandateIdAndUserId(
                sipMandateId,
                userId,
                SipStatus.CANCELLED,
                null,
                OffsetDateTime.now()
        );

        if (updatedRows == 0) {
            throw new InvalidSipStateException("Failed to cancel SIP mandate. Please try again.");
        }

        return "SIP mandate CANCELLED successfully";
    }

    private Long getUserId(String customerId) {
        return userService.getUserIdByCustomerId(customerId);
    }

    private void validateCreateRequest(CreateSipMandateRequest r) {

        PlanSipConfigProjection planSipConfigProjection = schemePlanRepository.findSipConfigByPlanId(r.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Invalid planId; no such plan found"));

        BigDecimal minSipAmount = planSipConfigProjection.getMinSip();
        boolean isSipAllowed = planSipConfigProjection.getIsSipAllowed();

        if (!isSipAllowed) {
            throw new SipNotAllowedException("SIP is not allowed for the selected plan");
        }

        if (r.getSipDay() < 1 || r.getSipDay() > 28) {
            throw new InvalidSipConfigurationException("SIP day must be between 1 and 28");
        }

        if (r.getTotalInstallments() == null || r.getTotalInstallments() <= 5) {
            throw new InvalidSipConfigurationException("Total installments must be greater than 5");
        }

        if (r.getSipAmount() == null) {
            throw new InvalidSipConfigurationException("SIP amount is required");
        }

        if (minSipAmount == null) {
            return;
        }

        if (r.getSipAmount().compareTo(minSipAmount) < 0) {
            throw new InvalidSipConfigurationException("SIP amount must be at least " + minSipAmount);
        }
    }

    private OffsetDateTime computeFirstRunAt(int sipDay) {

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);

        LocalDate candidate = today.withDayOfMonth(sipDay);

        // If sipDay has passed or is today, schedule for next month
        if (!candidate.isAfter(today)) {
            candidate = candidate.plusMonths(1).withDayOfMonth(sipDay);
        }

        return candidate
                .atTime(10, 0) // execution time
                .atZone(zone)
                .toOffsetDateTime();
    }

    private LocalDate calculateEndDate(LocalDate startDate, Integer totalInstallments) {
        return startDate.plusMonths(totalInstallments.longValue() - 1);
    }
}
