package com.yolt.creditscoring.service.creditscore.storage;

import com.yolt.creditscoring.controller.admin.users.Based64;
import com.yolt.creditscoring.exception.CreditScoreReportNotFoundException;
import com.yolt.creditscoring.service.creditscore.model.CreditScoreReport;
import com.yolt.creditscoring.service.creditscore.model.CreditScoreReportRepository;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.OverviewInfoDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class CreditScoreStorageService {

    private final CreditScoreReportRepository creditScoreReportRepository;

    public void saveCreditScoreReportForGivenUser(@Valid ReportSaveDTO creditScoreReportDTO,
                                                  ReportSignature calculatedSignature,
                                                  @NonNull UUID creditScoreUserId) {

        CreditScoreReport creditScoreReport = CreditScoreMapper.mapReportSaveToEntity(creditScoreReportDTO, calculatedSignature);
        creditScoreReport.setCreditScoreUserId(creditScoreUserId);

        creditScoreReport.getCreditScoreMonthly()
                .forEach(c -> c.setCreditScoreReport(creditScoreReport));

        creditScoreReportRepository.save(creditScoreReport);
    }

    public void deleteByCreditScoreUserId(UUID userId) {
        creditScoreReportRepository.deleteByCreditScoreUserId(userId);
    }

    public UUID getCreditScoreReportIdByUser(CreditScoreUserDTO creditScoreUser) {
        return creditScoreReportRepository.getCreditScoreReportIDByUserId(creditScoreUser.getId())
                .orElseThrow(() -> new CreditScoreReportNotFoundException(creditScoreUser.getStatus(), "Report not found for user with ID: " + creditScoreUser.getId()));
    }

    public Optional<UUID> findCreditScoreReportIdByUserId(UUID userId) {
        return creditScoreReportRepository.getCreditScoreReportIDByUserId(userId);
    }

    public Optional<BankAccountDetailsDTO> getCreditScoreReportBankAccountDetails(UUID userId) {
        return creditScoreReportRepository.findByCreditScoreUserId(userId)
                .map(creditScoreReport -> CreditScoreMapper.mapCreditScoreReportToAdminReportDTO(creditScoreReport, userId));
    }

    public ReportSignature getReportSignature(CreditScoreUserDTO creditScoreUserDTO) {
        UUID userId = creditScoreUserDTO.getId();
        return creditScoreReportRepository.findByCreditScoreUserId(userId)
                .map(creditScoreReport -> ReportSignature.builder()
                        .keyId(creditScoreReport.getSignatureKeyId())
                        .signature(Based64.fromEncoded(creditScoreReport.getSignature()))
                        .jsonPaths(creditScoreReport.getSignatureJsonPaths())
                        .build())
                .orElseThrow(() -> new CreditScoreReportNotFoundException(creditScoreUserDTO.getStatus(), "Report was not found with ID: " + userId));
    }

    public Set<MonthlyAdminReportDTO> getCreditScoreMonthsDTO(@NonNull CreditScoreUserDTO user) {
        UUID userId = user.getId();
        return creditScoreReportRepository.findByCreditScoreUserId(userId)
                .map(creditScoreReport -> CreditScoreMapper.mapCreditScoreMonthlyReportToMonthlyAdminReportDTO(creditScoreReport.getCreditScoreMonthly()))
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "Report was not found with ID: " + userId));
    }

    public @Valid OverviewInfoDTO getCreditScoreOverviewInfoDTO(@NonNull CreditScoreUserDTO user) {
        UUID userId = user.getId();
        return creditScoreReportRepository.findByCreditScoreUserId(userId)
                .map(CreditScoreMapper::mapCreditScoreReportToOverviewInfoDTO)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "Report was not found with ID: " + userId));
    }
}
