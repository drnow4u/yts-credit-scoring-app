package com.yolt.creditscoring.configuration.security.user;


import com.yolt.creditscoring.controller.exception.ControllerExceptionHandlers;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.controller.exception.ErrorType;
import com.yolt.creditscoring.exception.UnauthorizedException;
import com.yolt.creditscoring.service.user.UserStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_SELECT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.client.ClientController.CLIENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.creditscore.CreditScoreReportController.*;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.PRIVACY_POLICY_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.TERMS_CONDITIONS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.site.SiteController.*;

/**
 * Additional verification filter that is executed after
 * {@link com.yolt.creditscoring.configuration.security.user.UserJwtAuthorizationFilter}
 * for the credit score user.
 * <p>
 * Is responsible for checking if the invitation status is correct to allow user to view given resource.
 * <p>
 * Should block every request for credit score user resource when invitation status is EXPIRED, REFUSED or COMPLETED.
 * <p>
 * Additional resources should the allows REFUSED status are listed in ENDPOINTS_THAT_ALLOWS_REFUSED_STATUS
 */
@Slf4j
@RequiredArgsConstructor
public class UserFlowVerificationFilter extends OncePerRequestFilter {

    private final UserStorageService userStorageService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            if (!isStatusValidToContinueFlow(request.getRequestURI())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                ControllerExceptionHandlers.serializeErrorResponse(request, response, new ErrorResponseDTO(ErrorType.FLOW_ENDED), "User status invalid for given endpoint");
            } else {
                filterChain.doFilter(request, response);
            }

        } catch (UnauthorizedException e) {
            // Case when user was not authenticated in previous JwtAuthorizationFilter
            // Applies to all open resources - e.g. token exchange
            filterChain.doFilter(request, response);
        }
    }

    private boolean isStatusValidToContinueFlow(String endpoint) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            final CreditScoreUserPrincipal principal = (CreditScoreUserPrincipal) authentication.getPrincipal();

            return switch (endpoint) {
                case TERMS_CONDITIONS_ENDPOINT,
                        PRIVACY_POLICY_ENDPOINT,
                        CLIENT_ENDPOINT,
                        USER_CONSENT_ENDPOINT -> userStorageService.isStatusesAllowedForConsentPage(principal.getUserId());
                case SITES_ENDPOINT, USER_SITE_ENDPOINT, SITES_CONNECT_ENDPOINT -> userStorageService.isSiteConnectAllowed(principal.getUserId());
                case USER_ACCOUNTS_ENDPOINT, USER_ACCOUNTS_SELECT_ENDPOINT -> userStorageService.isAccountAllowed(principal.getUserId());
                case CASHFLOW_OVERVIEW_FOR_USER, CASHFLOW_OVERVIEW_CONFIRM, CASHFLOW_OVERVIEW_REFUSE -> userStorageService.isOverviewAllowed(principal.getUserId());
                default -> false;
            };
        } catch (Exception e) {
            throw new UnauthorizedException();
        }
    }

}
