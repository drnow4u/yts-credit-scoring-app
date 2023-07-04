package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.exception.OAuth2EmailMismatchException;
import com.yolt.creditscoring.exception.OAuth2NotRegisteredAdminException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * When a instance of {@link AuthenticationException} is thrown
 * the {@link OAuth2AuthenticationFailureHandler} logic is triggered.
 * <p>
 * It redirects the user back to the login page
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final AdminAuditService adminAuditService;
    private final SemaEventService semaEventService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        if (exception instanceof OAuth2NotRegisteredAdminException oauthException) {
            log.error("Not registered admin tried to login, idp ID: " + getCensoredIdpId(oauthException.getIdpId()) + " provider: " + oauthException.getProvider());
            adminAuditService.adminNotRegisteredLogIn(oauthException.getIdpId(), oauthException.getProvider(), ((OAuth2NotRegisteredAdminException) exception));
            semaEventService.logNotRegisteredAdminLogin(oauthException.getIdpId(), oauthException.getProvider(), request.getRemoteAddr());
        } else if (exception instanceof OAuth2EmailMismatchException oauthException) {
            semaEventService.logNotMatchingAdminEmailWithIdpId(
                    oauthException.getClientID(),
                    oauthException.getIdpId(),
                    oauthException.getStoredClientAdminEmail(),
                    oauthException.getProvidedClientAdminEmail(),
                    oauthException.getProvider()
            );
        } else {
            log.error("OAuth2 Authentication Failure", exception);
        }
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        SecurityContextHolder.clearContext();
    }

    private String getCensoredIdpId(String idpId) {
        if (idpId.length() >= 4) {
            return idpId.substring(0, 2) + "***" + idpId.charAt(idpId.length() - 1);
        }
        return "***";
    }
}
