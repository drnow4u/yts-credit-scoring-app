package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private SemaEventService semaEventService;

    @Mock
    PrintWriter printWriter;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Test
    void shouldCreateJwtForClientAdmin() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        given(request.getContextPath()).willReturn("/app-context");
        given(response.getWriter()).willReturn(printWriter);
        given(authentication.getPrincipal()).willReturn(OAUTH_ADMIN_USER_CLIENT_ADMIN);
        given(jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .willReturn("JWT_TOKEN");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        then(httpCookieOAuth2AuthorizationRequestRepository)
                .should()
                .removeAuthorizationRequestCookies(request, response);

        then(jwtCreationService)
                .should(times(1))
                .createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN);
        verifyNoMoreInteractions(jwtCreationService);
        then(adminAuditService).should().adminLogIn(eq(OAUTH_ADMIN_USER_CLIENT_ADMIN), any(), any());
        then(semaEventService).should().logAdminLoginToApplication(OAUTH_ADMIN_USER_CLIENT_ADMIN);

        then(printWriter)
                .should()
                .print("""
                                {"access_token":"JWT_TOKEN",
                                "token_type": "bearer",
                                "expires_in": 3600
                                }
                        """);
        then(printWriter)
                .should()
                .flush();
    }

}
