package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_EXPIRATION_TIME_MINUTES;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_TYPE_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * After the IDP authentication is successful, and use is validate in {@link CustomOAuth2UserService}
 * The {@link OAuth2AuthenticationSuccessHandler} is responsible for JWT creation and redirecting it to frontend endpoint
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtCreationService jwtCreationService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final AdminAuditService adminAuditService;
    private final SemaEventService semaEventService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            return;
        }

        OAuth2AdminUser oAuth2AdminUser;
        if (authentication.getPrincipal() instanceof CustomOidcUserService.CfaOidcUser oidcUser) {
            oAuth2AdminUser = oidcUser.getOAuth2User();
        } else if (authentication.getPrincipal() instanceof OAuth2AdminUser oauthAdmin) {
            oAuth2AdminUser = oauthAdmin;
        } else {
            throw new IllegalStateException("Unable to determine the principal.");
        }

        this.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.getWriter().print(String.format("""
                        {"access_token":"%1s",
                        "token_type": "%2s",
                        "expires_in": %2d
                        }
                """, jwtCreationService.createAdminToken(oAuth2AdminUser), TOKEN_TYPE_PREFIX.toLowerCase(), TOKEN_EXPIRATION_TIME_MINUTES * 60));
        response.getWriter().flush();

        adminAuditService.adminLogIn(oAuth2AdminUser, request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT));
        semaEventService.logAdminLoginToApplication(oAuth2AdminUser);
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
