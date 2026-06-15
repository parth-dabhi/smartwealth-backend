package com.smartwealth.smartwealth_backend.service.notification;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationService {

    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to customerId={}", user.getCustomerId());

        String maskedMobile = maskMobile(user.getMobileNumber());
        String createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().format(DATE_FORMATTER)
                : "—";

        Map<String, Object> variables = Map.of(
                "fullName",     user.getFullName(),
                "customerId",   user.getCustomerId(),
                "email",        user.getEmail(),
                "maskedMobile", maskedMobile,
                "createdAt",    createdAt
        );

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Welcome to SmartWealth 🎉 — Your Account is Ready",
                "email/welcome",
                variables
        );
    }

    public void sendKycStatusEmail(String email, String fullName, String customerId,
                                   KycStatus newStatus, String remark, OffsetDateTime updatedAt) {

        log.info("Sending KYC status email to customerId={} status={}", customerId, newStatus);

        boolean isVerified = newStatus == KycStatus.VERIFIED;

        String subject = isVerified
                ? "✅ Your KYC is Verified — Start Investing on SmartWealth!"
                : "❌ KYC Verification Failed — Action Required";

        String formattedDate = updatedAt != null
                ? updatedAt.format(DATE_FORMATTER)
                : LocalDate.now().format(DATE_FORMATTER);

        Map<String, Object> variables = Map.of(
                "fullName",   fullName,
                "customerId", customerId,
                "status",     newStatus.name(),
                "remark",     remark != null ? remark : "",
                "updatedAt",  formattedDate
        );

        emailService.sendHtmlEmail(email, subject, "email/kyc-update", variables);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) return "XXXXXXXXXX";
        return "XXXXXX" + mobile.substring(mobile.length() - 4);
    }
}
