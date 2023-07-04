package com.yolt.creditscoring.controller.admin.alert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AlertControllerIT {

    private Appender<ILoggingEvent> mockAppender;
    private ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
    }

    @BeforeEach
    public void beforeEach() {
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        mockAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(ILoggingEvent.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(SemaEventLogger.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(mockAppender);
    }

    @AfterEach
    public void afterEach() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    void shouldCorrectlyCreateSemaAlertEventFromEndpointCallForFailedSignatureVerification() throws Exception {
        // When
        ResultActions perform = mvc.perform(post("/api/admin/log/signature")
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"ba459bdb-5032-43ff-a339-500e9b20cf26\", \"signature\": \"MTIzNDU2Nzg5MA==\"}"));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
        List<ILoggingEvent> values = captorLoggingEvent.getAllValues();
        ILoggingEvent semaLog = values.get(0);
        assertThat(semaLog.getMessage()).isEqualTo("Verification of report signature failed on the frontend application");
        assertThat(semaLog.getMarker().toString()).contains("log_type=SEMA");
        assertThat(semaLog.getMarker().toString()).contains("sema_type=com.yolt.creditscoring.service.securitymodule.semaevent.InvalidSignatureSemaEvent");
        assertThat(semaLog.getMarker().toString()).contains("alarmTriggeredBy=c94b8a73-258f-4896-951f-79f981df592c");
        assertThat(semaLog.getMarker().toString()).contains("signature=MTIzNDU2Nzg5MA==");
        assertThat(semaLog.getMarker().toString()).contains("clientId=0b4cee11-0bd6-4e86-806f-45c913ad7bd5");
        assertThat(semaLog.getMarker().toString()).contains("userId=ba459bdb-5032-43ff-a339-500e9b20cf26");
    }
}