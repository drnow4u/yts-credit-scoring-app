package com.yolt.creditscoring.service.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import nl.ing.lovebird.logging.AuditLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AdminAuditServiceTest {
    private Appender<ILoggingEvent> logAppender;

    @InjectMocks
    private AdminAuditService adminAuditService;

    @BeforeEach
    public void beforeEach() {
        logAppender = mock(Appender.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(AuditLogger.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(logAppender);
    }

    @AfterEach
    public void afterEach() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(logAppender);
    }

    @Test
    void shouldLogUserConsentInAuditLog() {
        // Given

        // When
        adminAuditService.adminLogIn(
                OAUTH_ADMIN_USER_CLIENT_ADMIN,
                SOME_CLIENT_ADMIN_IP,
                SOME_CLIENT_ADMIN_USER_AGENT);

        // Then
        assertAuditMessageLogged("Cashflow Analyser client admin login", """
                object={\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "adminId":"c94b8a73-258f-4896-951f-79f981df592c",\
                "idpId":"f242d4aa-4f4d-49eb-ad92-10eb2f4c65d9",\
                "adminEmail":"adminuser@test.com",\
                "details":{\
                "ipAddress":"127.0.0.1",\
                "userAgent":"Chrome"\
                }\
                }\
                """);
    }

    @Test
    void shouldLogInviteNewUser() {
        // When
        adminAuditService.inviteNewUser(
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                SOME_USER_ID,
                SOME_USER_NAME,
                SOME_USER_EMAIL,
                ClientAccessType.ADMIN);

        // Then
        assertAuditMessageLogged("Cashflow Analyser client invited user, access type: ADMIN", """
                object={\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "adminId":"c94b8a73-258f-4896-951f-79f981df592c",\
                "adminEmail":"adminuser@test.com",\
                "clientAccessType":"ADMIN",\
                "details":{\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "userName":"User Name",\
                "userEmail":"user@email.com"\
                }\
                }\
                """);
    }

    @Test
    void shouldLogReinviteUser() {
        // Given

        // When
        adminAuditService.reinviteUser(
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                SOME_USER_ID,
                SOME_USER_NAME,
                SOME_USER_EMAIL);

        // Then
        assertAuditMessageLogged("Cashflow Analyser client admin re-invited user", """
                object={\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "adminId":"c94b8a73-258f-4896-951f-79f981df592c",\
                "adminEmail":"adminuser@test.com",\
                "details":{\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "userName":"User Name",\
                "userEmail":"user@email.com"\
                }\
                }\
                """);
    }

    @Test
    void shouldLogAdminViewedCreditReport() {
        // Given

        // When
        adminAuditService.adminViewedCreditReport(
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                SOME_USER_ID);

        // Then
        assertAuditMessageLogged("Cashflow Analyser client admin viewed user report", """
                object={\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "adminId":"c94b8a73-258f-4896-951f-79f981df592c",\
                "adminEmail":"adminuser@test.com",\
                "details":{\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26"\
                }\
                }\
                """);
    }

    @Test
    void shouldLogAdminDeleteUser() {
        // Given

        // When
        adminAuditService.deleteUser(
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                SOME_USER_ID,
                SOME_USER_EMAIL,
                ClientAccessType.ADMIN);

        // Then
        assertAuditMessageLogged("Cashflow Analyser client deleted user", """
                object={\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "adminId":"c94b8a73-258f-4896-951f-79f981df592c",\
                "adminEmail":"adminuser@test.com",\
                "clientAccessType":"ADMIN",\
                "details":{\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "userEmail":"user@email.com"\
                }\
                }\
                """);
    }

    private void assertAuditMessageLogged(final String message, final String markerEntry) {
        var loggingCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        then(logAppender).should().doAppend(loggingCaptor.capture());

        var loggingEvent = loggingCaptor.getValue();
        assertThat(loggingEvent.getMessage()).isEqualTo(message);
        assertThat(loggingEvent.getMarker().toString()).contains(markerEntry);
    }
}
