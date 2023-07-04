package com.yolt.creditscoring.service.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yolt.creditscoring.service.user.CreditScoreUserConsentStorage;
import nl.ing.lovebird.logging.AuditLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserAuditServiceTest {
    private Appender<ILoggingEvent> logAppender;

    @InjectMocks
    private UserAuditService userAuditService;

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
        userAuditService.logUserConsentInAuditLog(
                CreditScoreUserConsentStorage.builder()
                        .userId(SOME_USER_ID)
                        .dateTimeConsent(OffsetDateTime.parse("2021-08-30T10:11:12Z"))
                        .userAddress("127.0.0.1")
                        .userAgent("Chrome")
                        .termsAndConditionId(SOME_T_AND_C.getId())
                        .privacyPolicyId(SOME_PRIVACY_POLICY.getId())
                        .build(),
                SOME_USER_EMAIL,
                SOME_CLIENT_ID
        );

        // Then
        assertAuditMessageLogged("Cashflow Analyser user consented", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{\
                "consentDateTime":"2021-08-30T10:11:12Z",\
                "email":"user@email.com",\
                "ipAddress":"127.0.0.1",\
                "userAgent":"Chrome",\
                "termsAndConditionId":"%s",\
                "privacyPolicyId":"%s"}\
                }\
                """.formatted(SOME_T_AND_C.getId(), SOME_PRIVACY_POLICY.getId()));
    }

    @Test
    void shouldLogUseInvitationLink() {
        // Given

        // When
        userAuditService.useInvitationLink(SOME_CLIENT_ID, SOME_USER_ID, SOME_USER_EMAIL);

        // Then
        assertAuditMessageLogged("Cashflow Analyser user used invitation link", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{"email":"user@email.com"}\
                }\
                """);
    }

    @Test
    void shouldLogBankSelected() {
        // Given

        // When
        userAuditService.logBankSelected(SOME_CLIENT_ID, SOME_USER_ID, SOME_USER_IP);

        // Then
        assertAuditMessageLogged("Cashflow Analyser user selected bank", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{"ipAddress":"127.0.0.1"}\
                }\
                """);
    }

    @Test
    void shouldLogAccountSelected() {
        // Given

        // When
        userAuditService.logAccountSelected(SOME_CLIENT_ID, SOME_USER_ID, SOME_YOLT_USER_ACCOUNT_ID);

        // Then
        assertAuditMessageLogged("Cashflow Analyser user selected account", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{"accountId":"e74fc2f3-847f-4004-93f5-1248d4655ed8"}\
                }\
                """);
    }

    @Test
    void shouldLogConfirmReportShare() {
        // Given

        // When
        userAuditService.logConfirmReportShare(SOME_CLIENT_ID, SOME_USER_ID);

        // Then
        assertAuditMessageLogged("Cashflow Analyser user confirm report to share", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{}\
                }\
                """);
    }

    @Test
    void shouldLogReportCalculated() {
        // Given

        // When
        userAuditService.logReportCalculated(SOME_CLIENT_ID, SOME_USER_ID, SOME_REPORT_SIGNATURE, SOME_REPORT_SIGNATURE_KEY_ID);

        // Then
        assertAuditMessageLogged("Cashflow Analyser report calculated for user", """
                object={\
                "userId":"ba459bdb-5032-43ff-a339-500e9b20cf26",\
                "clientId":"0b4cee11-0bd6-4e86-806f-45c913ad7bd5",\
                "details":{"signature":"123456789w==","kid":"0a07c523-86a9-4bf9-9e0f-976beb37bcea"}\
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
