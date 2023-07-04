package com.yolt.creditscoring.service.audit;

import com.yolt.creditscoring.controller.admin.users.Based64;
import com.yolt.creditscoring.service.user.CreditScoreUserConsentStorage;
import lombok.NonNull;
import nl.ing.lovebird.logging.AuditLogger;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class UserAuditService {
    public void logUserConsentInAuditLog(@NonNull CreditScoreUserConsentStorage user, @NonNull String userEmail, @NonNull UUID clientId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        AuditLogger.logSuccess("Cashflow Analyser user consented", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(user.getUserId())
                .detail("consentDateTime", fmt.format(user.getDateTimeConsent()))
                .detail("email", userEmail)
                .detail("ipAddress", user.getUserAddress())
                .detail("userAgent", user.getUserAgent())
                .detail("termsAndConditionId", user.getTermsAndConditionId().toString())
                .detail("privacyPolicyId", user.getPrivacyPolicyId().toString())
                .build());
    }

    public void useInvitationLink(@NonNull UUID clientId, @NonNull UUID userId, @NonNull String email) {
        AuditLogger.logSuccess("Cashflow Analyser user used invitation link", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(userId)
                .detail("email", email)
                .build());
    }

    public void logBankSelected(@NonNull UUID clientId, @NonNull UUID userId, @NonNull String userIpAddress) {
        AuditLogger.logSuccess("Cashflow Analyser user selected bank", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(userId)
                .detail("ipAddress", userIpAddress)
                .build());
    }

    public void logAccountSelected(@NonNull UUID clientId, @NonNull UUID userId, @NonNull UUID accountId) {
        AuditLogger.logSuccess("Cashflow Analyser user selected account", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(userId)
                .detail("accountId", accountId.toString())
                .build());
    }

    public void logConfirmReportShare(@NonNull UUID clientId, @NonNull UUID userId) {
        AuditLogger.logSuccess("Cashflow Analyser user confirm report to share", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(userId)
                .build());
    }

    public void logReportCalculated(@NonNull UUID clientId, @NonNull UUID userId, @NonNull Based64 signature, @NonNull UUID keyId) {
        AuditLogger.logSuccess("Cashflow Analyser report calculated for user", UserAuditDTO.builder()
                .clientId(clientId)
                .userId(userId)
                .detail("signature", signature.toString())
                .detail("kid", keyId.toString())
                .build());
    }
}
