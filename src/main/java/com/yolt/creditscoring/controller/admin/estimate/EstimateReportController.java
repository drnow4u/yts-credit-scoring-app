package com.yolt.creditscoring.controller.admin.estimate;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.usecase.EstimateReportUseCase;
import com.yolt.creditscoring.usecase.dto.RiskClassificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class EstimateReportController {

    public static final String GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT = "/api/admin/users/{creditScoreUserId}/credit-report/risk-classification";

    private final EstimateReportUseCase estimateReportUseCase;

    @GetMapping(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT)
    public @Valid RiskClassificationDTO getRiskReportByUserId(@PathVariable UUID creditScoreUserId,
                                                              @ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return estimateReportUseCase.getUserRiskScore(
                creditScoreUserId,
                principal.getClientId());
    }

    /**
     * To handle situation when feature toggle was switch on after report had been generated.
     */
    @ExceptionHandler(EstimateReportNotFound.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> handleNoSuchElementFoundException(EstimateReportNotFound exception) {
        log.warn(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Not found");
    }

}
