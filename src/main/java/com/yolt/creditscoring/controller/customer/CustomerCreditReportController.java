package com.yolt.creditscoring.controller.customer;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.customer.ClientTokenPrincipal;
import com.yolt.creditscoring.controller.admin.estimate.EstimateReportNotFound;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.exception.CreditScoreReportNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.ReportDownloadDataDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.usecase.*;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminOverviewResponseDTO;
import com.yolt.creditscoring.usecase.dto.RiskClassificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.springdoc.annotations.ExternalApi;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN)
public class CustomerCreditReportController {
    public static final String FETCH_USER_REPORT_V1_ENDPOINT = "/api/customer/v1/users/{creditScoreUserId}/report";

    private final ReportUseCase reportUseCase;
    private final ReportOverviewUseCase reportOverviewUseCase;
    private final ReportMonthsUseCase reportMonthsUseCase;
    private final ReportCategoriesUseCase reportCategoriesUseCase;
    private final EstimateReportUseCase estimateReportUseCase;
    private final AdminAuditService adminAuditService;

    @Operation(
            summary = "Get overview report",
            description = "Get all the data of the user in one report.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful",
                            content = { @Content(schema = @Schema(implementation = AllInOneReport.class)) }
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized.",
                            content = { @Content(schema = @Schema(implementation = ErrorResponseDTO.class)) }
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User (creditScoreUserId) not found",
                            content = { @Content(schema = @Schema(implementation = ErrorResponseDTO.class)) }
                    ),
            })
    @PreAuthorize("hasAuthority('" + ClientTokenPermission.Permissions.DOWNLOAD_REPORT + "')")
    @ExternalApi
    @GetMapping(FETCH_USER_REPORT_V1_ENDPOINT)
    public AllInOneReport getAllInOneReportByUserId(@PathVariable UUID creditScoreUserId,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal ClientTokenPrincipal principal) {
        ReportDownloadDataDTO reportDownloadDataForUser;
        TogglesDTO toggles;
        CreditScoreAdminOverviewResponseDTO overviewJson = null;
        BankAccountDetailsDTO bankAccountDetails = null;
        List<SMECategoryDTO> categoriesCsv = null;
        Set<MonthlyAdminReportDTO> monthsCsv = null;
        RiskClassificationDTO estimateJson = null;
        InvitationStatus status = InvitationStatus.COMPLETED;

        try {
            reportDownloadDataForUser = reportUseCase.getUserReportDownloadDataForAllInOneReport(creditScoreUserId, principal.getClientId());
            toggles = reportDownloadDataForUser.getToggles();
        } catch (CreditScoreReportNotFoundException e) {
            log.info(e.getMessage(), e);
            return new AllInOneReport(overviewJson, bankAccountDetails, categoriesCsv, monthsCsv, estimateJson, e.getStatus());
        }

        if (toggles.isOverviewFeatureToggle()) {
            CreditScoreAdminOverviewResponseDTO creditScoreOverviewJson = reportOverviewUseCase.getUserCreditScore(creditScoreUserId, principal.getClientId());
            try {
                bankAccountDetails = reportUseCase.getUserBankAccountDetails(creditScoreUserId, principal.getClientId());
            } catch (CreditScoreReportNotFoundException e) {
                log.info(e.getMessage(), e);
                status = e.getStatus();
            }
            overviewJson = creditScoreOverviewJson;
        }

        if (toggles.isCategoryFeatureToggle()) {
            try {
                categoriesCsv = reportCategoriesUseCase.getUserCategoriesForAllInOneReport(creditScoreUserId, principal.getClientId());
            } catch (CreditScoreReportNotFoundException e) {
                log.info(e.getMessage(), e);
                status = e.getStatus();
            }
        }

        if (toggles.isMonthsFeatureToggle()) {
            monthsCsv = reportMonthsUseCase.getUserCreditScoreMonths(principal.getClientId(), creditScoreUserId);
        }

        if (toggles.isEstimateFeatureToggle()) {
            try {
                estimateJson = estimateReportUseCase.getUserRiskScore(creditScoreUserId, principal.getClientId());
            } catch (EstimateReportNotFound e) {
                log.warn("Estimate report was missing for customer api.");
            }
        }

        adminAuditService.adminFetchCreditReport(principal.getClientId(), principal.getTokenId(), principal.getEmail(), creditScoreUserId);

        return new AllInOneReport(overviewJson, bankAccountDetails, categoriesCsv, monthsCsv, estimateJson, status);
    }

    public record AllInOneReport(CreditScoreAdminOverviewResponseDTO overview,
                                 BankAccountDetailsDTO accountDetails,
                                 List<SMECategoryDTO> categories,
                                 Set<MonthlyAdminReportDTO> months,
                                 RiskClassificationDTO riskClassification,
                                 InvitationStatus userInvitationStatus) {
    }
}
