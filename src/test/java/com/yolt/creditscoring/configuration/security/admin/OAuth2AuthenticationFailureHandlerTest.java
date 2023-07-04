package com.yolt.creditscoring.configuration.security.admin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yolt.creditscoring.exception.OAuth2EmailMismatchException;
import com.yolt.creditscoring.exception.OAuth2NotRegisteredAdminException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    protected Appender<ILoggingEvent> mockAppender;
    protected ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private SemaEventService semaEventService;

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    @BeforeEach
    public void beforeEach() {
        mockAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(ILoggingEvent.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    public void afterEach() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void shouldRedirectToAdminFrontEndLoginPage() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        failureHandler.onAuthenticationFailure(request, response, mock(AuthenticationException.class));

        // Then
        then(httpCookieOAuth2AuthorizationRequestRepository)
                .should()
                .removeAuthorizationRequestCookies(request, response);

        then(response)
                .should()
                .setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @ParameterizedTest
    @CsvSource({
            "abcd,                                      ab***d",
            "abcde,                                     ab***e",
            "f242d4aa-4f4d-49eb-ad92-10eb2f4c65d9,      f2***9"
    })
    void shouldLogNotRegisteredAdminLogin(String idpId, String censoredIdpId) throws Exception{
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        given(request.getRemoteAddr()).willReturn(SOME_USER_IP);

        OAuth2NotRegisteredAdminException exception = new OAuth2NotRegisteredAdminException(idpId, "microsoft");

        // When
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent log = values.get(0);
        assertThat(log.getFormattedMessage()).isEqualTo("Not registered admin tried to login, idp ID: " + censoredIdpId + " provider: microsoft");

        then(adminAuditService).should().adminNotRegisteredLogIn(idpId, "microsoft", exception);
        then(semaEventService).should().logNotRegisteredAdminLogin(
                idpId,
                "microsoft",
                SOME_USER_IP
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "abc"})
    void shouldLogNotRegisteredAdminLoginWhenIdpIdHasLessThen4Characters(String idpId) throws Exception{
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        given(request.getRemoteAddr()).willReturn(SOME_USER_IP);

        OAuth2NotRegisteredAdminException exception = new OAuth2NotRegisteredAdminException(idpId, "microsoft");

        // When
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent log = values.get(0);
        assertThat(log.getFormattedMessage()).isEqualTo("Not registered admin tried to login, idp ID: *** provider: microsoft");

        then(adminAuditService).should().adminNotRegisteredLogIn(idpId, "microsoft", exception);
        then(semaEventService).should().logNotRegisteredAdminLogin(
                idpId,
                "microsoft",
                SOME_USER_IP
        );
    }

    @Test
    void shouldSendSemaEventForEmailMismatch() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        OAuth2EmailMismatchException exception = OAuth2EmailMismatchException.builder()
                .clientID(SOME_CLIENT_ID)
                .idpId(SOME_CLIENT_ADMIN_IDP_ID)
                .storedClientAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .providedClientAdminEmail(SOME_CLIENT_2_ADMIN_EMAIL)
                .provider("google")
                .build();

        // When
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Then
        then(semaEventService).should().logNotMatchingAdminEmailWithIdpId(
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_IDP_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                SOME_CLIENT_2_ADMIN_EMAIL,
                "google"
        );

    }
}
