package com.yolt.creditscoring.controller.admin.estimate;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientLanguage;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.storage.EstimateEntity;
import com.yolt.creditscoring.service.estimate.storage.EstimateRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN_2;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.estimate.EstimateReportController.GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class EstimateReportControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAdminRepository clientAdminRepository;

    @Autowired
    private EstimateRepository estimateRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        estimateRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
//       Risk classification,  Expected annualized default rate(min, max)
            "              A,                                   0.0, 0.5",
            "              C,                                   1.5, 2.5",
            "              F,                                   4.5, 5.5",
            "              J,                                   8.5,    ",
    })
    void shouldAdminWithFeatureToggleOnBeAbleToGetRiskReport(RiskClassification grade,
                                                             Double expectedRiskLower,
                                                             Double expectedRiskUpper) throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(grade)
                .setScore(10)
                .setStatus(PdStatus.COMPLETED));

        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));


        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateLower", equalTo(expectedRiskLower)))
                .andExpect(jsonPath("$.rateUpper", equalTo(expectedRiskUpper)))
                .andExpect(jsonPath("$.grade", equalTo(grade.toString())))
                .andExpect(jsonPath("$.status", equalTo("COMPLETED")));
    }

    @Test
    void shouldAdminWithFeatureToggleOnBeAbleToGetRiskReportWithNotEnoughTransactionStatus() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(null)
                .setScore(null)
                .setStatus(PdStatus.ERROR_NOT_ENOUGH_TRANSACTIONS));

        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateLower", nullValue()))
                .andExpect(jsonPath("$.rateUpper", nullValue()))
                .andExpect(jsonPath("$.grade", nullValue()))
                .andExpect(jsonPath("$.status", equalTo("ERROR_NOT_ENOUGH_TRANSACTIONS")));
    }

    /**
     * This situation can happen when feature toggle for client is switch on after report is calculated for user.
     */
    @Test
    void shouldClientWithFeatureToggleOnAfterReportWasCalculatedBeGetErrorWhenReportWasNotCalculated() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNoContent())
                .andExpect(content().string("Not found"));
    }

    @Test
    void shouldAdminWithFeatureToggleOnBeAbleToGetRiskReportWithErrorWhenReportWasNotCalculated() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(null)
                .setScore(null)
                .setStatus(PdStatus.ERROR));


        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateLower", nullValue()))
                .andExpect(jsonPath("$.rateUpper", nullValue()))
                .andExpect(jsonPath("$.grade", nullValue()))
                .andExpect(jsonPath("$.status", equalTo("ERROR")));
    }

    @Test
    void shouldAdminWithFeatureToggleOffNotBeAbleToGetRiskReport() throws Exception {
        // Given
        ClientEntity client = new ClientEntity();
        client.setId(SOME_CLIENT_ID_2);
        client.setAdditionalTextReport(SOME_CLIENT_ADDITIONAL_TEXT);
        client.setPDScoreFeatureToggle(false);
        client.setSiteTags("NL");
        client.setName(SOME_CLIENT_2_NAME);
        client.setDefaultLanguage(ClientLanguage.NL);
        clientRepository.save(client);

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setId(SOME_CLIENT_2_ADMIN_ID);
        clientAdmin.setClientId(SOME_CLIENT_ID_2);
        clientAdmin.setEmail(SOME_CLIENT_2_ADMIN_EMAIL);
        clientAdmin.setIdpId(SOME_CLIENT_2_ADMIN_IDP_ID);
        clientAdmin.setAuthProvider(AuthProvider.GITHUB);
        clientAdminRepository.save(clientAdmin);

        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID_2)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN_2)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not allowed"));
    }

    @Test
    void shouldAdminWithFeatureToggleOnNotBeAbleToGetRiskReportForOtherUser() throws Exception {
        // Given
        ClientEntity client = new ClientEntity();
        client.setId(SOME_CLIENT_ID_2);
        client.setAdditionalTextReport(SOME_CLIENT_ADDITIONAL_TEXT);
        client.setPDScoreFeatureToggle(true);
        client.setSiteTags("NL");
        client.setName(SOME_CLIENT_2_NAME);
        client.setDefaultLanguage(ClientLanguage.NL);
        clientRepository.save(client);

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setId(SOME_CLIENT_2_ADMIN_ID);
        clientAdmin.setClientId(SOME_CLIENT_ID_2);
        clientAdmin.setEmail(SOME_CLIENT_2_ADMIN_EMAIL);
        clientAdmin.setIdpId(SOME_CLIENT_2_ADMIN_IDP_ID);
        clientAdmin.setAuthProvider(AuthProvider.GITHUB);
        clientAdminRepository.save(clientAdmin);

        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(GET_USER_RISK_CLASSIFICATION_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN_2)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", notNullValue()))
                .andExpect(jsonPath("$.errorType", equalTo("USER_NOT_FOUND")));
    }

}
