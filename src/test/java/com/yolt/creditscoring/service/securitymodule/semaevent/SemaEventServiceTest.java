package com.yolt.creditscoring.service.securitymodule.semaevent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SemaEventServiceTest {

    private Appender<ILoggingEvent> logAppender;
    private ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

    @InjectMocks
    private SemaEventService semaEventService;

    @BeforeEach
    public void beforeEach() {
        logAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(ILoggingEvent.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(SemaEventLogger.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(logAppender);
    }

    @AfterEach
    public void afterEach() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(logAppender);
    }

    @Test
    void shouldLogAdminLoginToApplication() {
        // When
        semaEventService.logAdminLoginToApplication(OAUTH_ADMIN_USER_CLIENT_ADMIN);

        // Then
        then(logAppender).should(times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent messageLog = values.get(0);
        assertThat(messageLog.getMessage()).isEqualTo("Admin with following idpId has logged into application: " + SOME_CLIENT_ADMIN_IDP_ID);
        assertThat(messageLog.getMarker().toString()).contains("""
                log_type=SEMA, \
                sema_type=com.yolt.creditscoring.service.securitymodule.semaevent.AdminLoginSemaEvent, \
                idpId=f242d4aa-4f4d-49eb-ad92-10eb2f4c65d9, \
                clientId=0b4cee11-0bd6-4e86-806f-45c913ad7bd5, \
                adminEmail=adminuser@test.com""");
    }

    @Test
    void shouldLogUserInvitation() {
        // When
        semaEventService.logUserInvitation(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID);

        // Then
        then(logAppender).should(times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent messageLog = values.get(0);
        assertThat(messageLog.getMessage()).isEqualTo("User invited for client ID: " + SOME_CLIENT_ID + " by admin ID: " + SOME_CLIENT_ADMIN_ID);
        assertThat(messageLog.getMarker().toString()).contains("""
                log_type=SEMA, \
                sema_type=com.yolt.creditscoring.service.securitymodule.semaevent.InvitationSpikesSemaEvent, \
                adminId=c94b8a73-258f-4896-951f-79f981df592c, \
                clientId=0b4cee11-0bd6-4e86-806f-45c913ad7bd5""");
    }
}