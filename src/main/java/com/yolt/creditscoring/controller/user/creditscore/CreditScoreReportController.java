package com.yolt.creditscoring.controller.user.creditscore;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.usecase.CalculateCreditScoreUseCase;
import com.yolt.creditscoring.usecase.ConfirmCreditScoreReportUseCase;
import com.yolt.creditscoring.usecase.dto.CreditScoreUserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
public class CreditScoreReportController {

    public static final String CASHFLOW_OVERVIEW_FOR_USER = "/api/user/cashflow-overview";
    public static final String CASHFLOW_OVERVIEW_CONFIRM = "/api/user/cashflow-overview/confirm";
    public static final String CASHFLOW_OVERVIEW_REFUSE = "/api/user/cashflow-overview/refuse";

    private final ConfirmCreditScoreReportUseCase confirmCreditScoreReportUseCase;
    private final ReportCalculatorRunnerService reportCalculatorRunnerService;
    private final CalculateCreditScoreUseCase creditScoreUseCase;

    @GetMapping(CASHFLOW_OVERVIEW_FOR_USER)
    public @Valid CreditScoreUserResponseDTO fetchCreditReportForTheUser(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return confirmCreditScoreReportUseCase.getReportForUser(principal.getUserId());
    }

    @PostMapping(CASHFLOW_OVERVIEW_CONFIRM)
    public RedirectUrlDTO confirmReportShare(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        confirmCreditScoreReportUseCase.confirmReportShare(principal.getUserId(), principal.getClientId());

        reportCalculatorRunnerService.executeAsyncReportCalculation(
                () -> creditScoreUseCase.calculateCreditReportForGivenAccount(principal.getUserId()));

        return new RedirectUrlDTO(confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(principal.getUserId(), principal.getClientId(), true));
    }

    @PostMapping(CASHFLOW_OVERVIEW_REFUSE)
    public RedirectUrlDTO refuseReportShare(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        confirmCreditScoreReportUseCase.refuseReportShare(principal.getUserId());
        return new RedirectUrlDTO(confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(principal.getUserId(), principal.getClientId(), false));
    }

    public record RedirectUrlDTO (String redirectUrl) {}
}
