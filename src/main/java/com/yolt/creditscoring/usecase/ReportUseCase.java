package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.exception.CreditScoreReportNotFoundException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.ReportDownloadDataDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.InvalidSignatureDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.securitymodule.signature.SignatureService;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class ReportUseCase {

    private final UserStorageService userStorageService;
    private final CreditScoreStorageService creditScoreStorageService;
    private final ClientStorageService clientService;
    private final SignatureService signatureService;
    private final SemaEventService semaEventService;
    private final AdminAuditService adminAuditService;

    public @Valid CreditScoreAdminResponseDTO getUserCreditScore(UUID userId, UUID clientId, UUID adminId, String adminEmail) {
        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        final boolean hasSignatureVerification = clientService.hasSignatureVerificationFeature(clientId);

        BankAccountDetailsDTO bankAccountDetailsDTO = creditScoreStorageService.getCreditScoreReportBankAccountDetails(userId)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "credit score report was not found for user ID: " + userId));

        ReportSignature reportSignature = creditScoreStorageService.getReportSignature(user);

        CreditScoreAdminResponseDTO creditScoreResponseDTO = CreditScoreAdminResponseDTO.builder()
                .userEmail(user.getEmail())
                .adminReport(bankAccountDetailsDTO)
                .signature(reportSignature.getSignature())
                .signatureJsonPaths(reportSignature.getJsonPaths())
                .publicKey(signatureService.getPublicKeyModulus(reportSignature.getKeyId()))
                .shouldVerifiedSignature(hasSignatureVerification)
                .build();

        if (!signatureService.verify(bankAccountDetailsDTO,
                ReportSignature.builder()
                        .signature(reportSignature.getSignature())
                        .keyId(reportSignature.getKeyId())
                        .jsonPaths(reportSignature.getJsonPaths())
                        .build())) {
            semaEventService.logIncorrectSignature(InvalidSignatureDTO
                            .builder()
                            .userId(userId)
                            .signature(reportSignature.getSignature())
                            .build(),
                    userId, clientId);
        }
        adminAuditService.adminViewedCreditReport(clientId, adminId, adminEmail, userId);
        return creditScoreResponseDTO;
    }

    public ReportDownloadDataDTO getUserReportDownloadDataForAllInOneReport(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = validateUser(userId, clientId);
        final TogglesDTO featureToggles = clientService.getFeatureToggles(clientId);
        final BankAccountDetailsDTO bankAccountDetailsDTO = getAdminReportDTO(userId, clientId, user);

        return getReportDownloadDataDTO(featureToggles, bankAccountDetailsDTO);
    }

    public ReportDownloadDataDTO getUserReportDownloadData(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = validateUser(userId, clientId);
        final TogglesDTO featureToggles = clientService.getFeatureToggles(clientId);

        final BankAccountDetailsDTO bankAccountDetailsDTO = creditScoreStorageService.getCreditScoreReportBankAccountDetails(userId)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "Report was not found for user with ID: " + userId));

        return getReportDownloadDataDTO(featureToggles, bankAccountDetailsDTO);
    }

    public BankAccountDetailsDTO getUserBankAccountDetails(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = validateUser(userId, clientId);
        return getAdminReportDTO(userId, clientId, user);
    }

    private BankAccountDetailsDTO getAdminReportDTO(UUID userId, UUID clientId, CreditScoreUserDTO user) {
        return creditScoreStorageService.getCreditScoreReportBankAccountDetails(userId)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "User was not found with userID: " + userId + "and clientID: " + clientId));
    }

    private ReportDownloadDataDTO getReportDownloadDataDTO(TogglesDTO featureToggles, BankAccountDetailsDTO bankAccountDetailsDTO) {
        return ReportDownloadDataDTO.builder()
                .toggles(featureToggles)
                .currency(bankAccountDetailsDTO.getCurrency())
                .build();
    }

    private CreditScoreUserDTO validateUser(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }
        return user;
    }
}
