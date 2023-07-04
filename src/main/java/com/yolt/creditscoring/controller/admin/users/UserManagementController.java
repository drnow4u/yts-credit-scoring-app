package com.yolt.creditscoring.controller.admin.users;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.controller.admin.estimate.EstimateReportNotFound;
import com.yolt.creditscoring.controller.exception.FormValidationErrorResponse;
import com.yolt.creditscoring.controller.exception.Violation;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.ReportDownloadDataDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.usecase.*;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminMonthsResponseDTO;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminOverviewResponseDTO;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.yolt.creditscoring.utility.download.DownloadReportMapper.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class UserManagementController {

    public static final String GET_USERS_ENDPOINT = "/api/admin/users";
    public static final String INVITE_USER_ENDPOINT = "/api/admin/users/invite";
    public static final String RE_INVITE_USER_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/resend-invite";
    public static final String DELETE_USER_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}";
    public static final String GET_USER_REPORT_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report";
    public static final String GET_USER_OVERVIEW_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report/overview";
    public static final String GET_USER_MONTHS_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report/months";
    public static final String GET_USER_CATEGORIES_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report/categories";
    public static final String DOWNLOAD_REPORT_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report/download";

    private final UserManagementUseCase userManagementUseCase;
    private final ReportUseCase reportUseCase;
    private final ReportOverviewUseCase reportOverviewUseCase;
    private final ReportMonthsUseCase reportMonthsUseCase;
    private final ReportCategoriesUseCase reportCategoriesUseCase;
    private final EstimateReportUseCase estimateReportUseCase;

    @GetMapping(GET_USERS_ENDPOINT)
    public ResponseEntity<List<ViewUserDTO>> viewUsersForLoggedClient(Pageable pageable,
                                                                      @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        Page<ViewUserDTO> users = userManagementUseCase.viewAllUsersByClientId(pageable, principal.getClientId());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Total-Count", String.valueOf(users.getTotalElements()));
        responseHeaders.set("X-Pagination-Page", String.valueOf(users.getNumber()));
        responseHeaders.set("X-Pagination-Pages", String.valueOf(users.getTotalPages()));
        responseHeaders.set("X-Pagination-PageSize", String.valueOf(users.getSize()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(users.getContent());
    }

    @PostMapping(INVITE_USER_ENDPOINT)
    public void inviteUser(@Valid @RequestBody InviteUserDTO inviteUserDTO,
                           @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        userManagementUseCase.inviteNewUser(
                inviteUserDTO,
                principal.getClientId(),
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                principal.getAdminId(),
                principal.getEmail(),
                principal.getClientAccessType());
    }

    @PutMapping(RE_INVITE_USER_BY_USERID_ENDPOINT)
    public void resendUserInvite(@PathVariable UUID creditScoreUserId,
                                 @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        userManagementUseCase.resendUserInvite(
                creditScoreUserId,
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(),
                principal.getClientId(),
                principal.getAdminId(),
                principal.getEmail());
    }

    @DeleteMapping(DELETE_USER_BY_USERID_ENDPOINT)
    public void deleteUser(@PathVariable UUID creditScoreUserId,
                           @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        userManagementUseCase.deleteUserByUserID(
                creditScoreUserId,
                principal.getClientId(),
                principal.getAdminId(),
                principal.getEmail(),
                principal.getClientAccessType());
    }

    @GetMapping(GET_USER_REPORT_BY_USERID_ENDPOINT)
    public @Valid
    CreditScoreAdminResponseDTO getCreditReportByUserId(@PathVariable UUID creditScoreUserId,
                                                        @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return reportUseCase.getUserCreditScore(
                creditScoreUserId,
                principal.getClientId(),
                principal.getAdminId(),
                principal.getEmail());
    }

    @GetMapping(GET_USER_OVERVIEW_BY_USERID_ENDPOINT)
    public @Valid
    CreditScoreAdminOverviewResponseDTO getCreditReportOverviewByUserId(@PathVariable UUID creditScoreUserId,
                                                                        @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return reportOverviewUseCase.getUserCreditScore(
                creditScoreUserId,
                principal.getClientId());
    }

    @GetMapping(GET_USER_MONTHS_BY_USERID_ENDPOINT)
    public CreditScoreAdminMonthsResponseDTO getCreditReportMonthsByUserId(@PathVariable UUID creditScoreUserId,
                                                                           @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return CreditScoreAdminMonthsResponseDTO.builder()
                .monthlyReports(reportMonthsUseCase.getUserCreditScoreMonths(principal.getClientId(), creditScoreUserId))
                .build();
    }

    @GetMapping(GET_USER_CATEGORIES_BY_USERID_ENDPOINT)
    public List<SMECategoryDTO> getReportAggregatedCategoriesByUserId(@PathVariable UUID creditScoreUserId,
                                                                      @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return reportCategoriesUseCase.getUserCategories(
                creditScoreUserId,
                principal.getClientId()
        );
    }

    @RequestMapping(value = DOWNLOAD_REPORT_BY_USERID_ENDPOINT, produces = "application/zip")
    public ResponseEntity<byte[]> getReportForDownloadByUserId(@PathVariable UUID creditScoreUserId,
                                                               @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        var out = new ByteArrayOutputStream();
        final ReportDownloadDataDTO reportDownloadDataForUser = reportUseCase.getUserReportDownloadData(creditScoreUserId, principal.getClientId());
        final TogglesDTO toggles = reportDownloadDataForUser.getToggles();

        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            if (toggles.isOverviewFeatureToggle()) {
                byte[] overviewJson = generateJson(reportOverviewUseCase.getUserCreditScore(creditScoreUserId, principal.getClientId()));
                addToZip(zip, "Overview.json", overviewJson);
            }

            if (toggles.isCategoryFeatureToggle()) {
                byte[] categoriesCsv = generateCsvForCategories(reportCategoriesUseCase.getUserCategories(creditScoreUserId, principal.getClientId()));
                addToZip(zip, "CategoriesReport.csv", categoriesCsv);
            }

            if (toggles.isMonthsFeatureToggle()) {
                byte[] monthsCsv = generateCsvForMonths(reportDownloadDataForUser.getCurrency(), reportMonthsUseCase.getUserCreditScoreMonths(principal.getClientId(), creditScoreUserId));
                var report = reportUseCase.getUserCreditScore(creditScoreUserId, principal.getClientId(), principal.getAdminId(), principal.getEmail());

                String signature = report.getSignature().toEncoded();
                addToZip(zip, toAvailableAccountNumber(report.getAdminReport())
                        + "_"
                        + report.getAdminReport().getNewestTransactionDate()
                        + "_"
                        + report.getAdminReport().getOldestTransactionDate()
                        + "_"
                        + signature.substring(0, 7)
                        + "_"
                        + signature.substring(signature.length() - 8)
                        + "_"
                        + "monthly.csv", monthsCsv);
            }

            if (toggles.isEstimateFeatureToggle()) {
                try {
                    byte[] estimateJson = generateJson(estimateReportUseCase.getUserRiskScore(creditScoreUserId, principal.getClientId()));
                    addToZip(zip, "EstimateReport.json", estimateJson);
                } catch (EstimateReportNotFound e) {
                    log.warn("Estimate report was missing for download.");
                }
            }
        } catch (IOException e) {
            log.warn("There was an error closing zip stream.");
        }

        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"report.zip\"")
                .header("Content-Type", "application/zip")
                .body(out.toByteArray());
    }

    private static String toAvailableAccountNumber(@NotNull BankAccountDetailsDTO accountReference) {
        if (accountReference.getIban() != null) return accountReference.getIban();
        if (accountReference.getSortCodeAccountNumber() != null) return accountReference.getSortCodeAccountNumber();
        if (accountReference.getBban() != null) return accountReference.getBban();
        if (accountReference.getMaskedPan() != null) return accountReference.getMaskedPan();

        log.warn("Every value in account reference was empty. Account will be filtered out");
        return null;
    }

    private void addToZip(ZipOutputStream zipOutputStream, String filename, byte[] content) {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(filename));
            zipOutputStream.write(content);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            log.error("There was an error creating report .zip file - {}.", filename);
        }
    }

    /**
     * An exception handler for this controller. We return validation errors to the frontend that relies on the validation messages and this structure.
     * We don't want to globally override the lovebird commons {@link nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers#handleMethodArgumentNotValidException(MethodArgumentNotValidException)}
     * more safe version that does not expose messages, but since we need them in the frontend, we make an exception for this controller.
     *
     * @param ex The exception
     * @return The response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public FormValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return FormValidationErrorResponse.builder()
                .violations(ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(field -> new Violation(field.getField(), field.getDefaultMessage()))
                        .toList())
                .build();
    }
}
