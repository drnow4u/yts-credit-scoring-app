package com.yolt.creditscoring.configuration.security.admin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yolt.creditscoring.exception.LogoutException;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.apache.http.HttpHeaders;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomLogoutHandlerTest {

    protected Appender<ILoggingEvent> mockAppender;
    protected ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

    @Mock
    private JwtCreationService jwtCreationService;

    @InjectMocks
    private CustomLogoutHandler customLogoutHandler;

    @BeforeEach
    public void beforeEach() {
        mockAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(ILoggingEvent.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(CustomLogoutHandler.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    public void afterEach() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void shouldCorrectlyLogJwtIdWhenLogging() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setJwtId("SOME_JWT_ID");

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtClaimsFromDecryptedJwt("SOME_ENCODED_JWT")).willReturn(jwtClaims);

        // When
        customLogoutHandler.logout(request, response, authentication);

        // Then
        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent log = values.get(0);
        assertThat(log.getFormattedMessage()).isEqualTo("User with JWT ID SOME_JWT_ID has logged out from the application");
    }

    @Test
    void shouldThrowLogoutExceptionWhenThereWouldBeAnErrorDuringLogoutPorcess() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtClaimsFromDecryptedJwt("SOME_ENCODED_JWT"))
                .willThrow(new RuntimeException("SOME_ERROR"));

        // When
        Throwable thrown = catchThrowable(() ->customLogoutHandler.logout(request, response, authentication));

        // Then
        assertThat(thrown).isInstanceOf(LogoutException.class);
        assertThat(thrown.getMessage()).isEqualTo("There was an error when user was logging out");

    }
}