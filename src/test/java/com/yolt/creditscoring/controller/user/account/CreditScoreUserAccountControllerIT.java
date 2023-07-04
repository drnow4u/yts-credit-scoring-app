package com.yolt.creditscoring.controller.user.account;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.creditscore.model.CreditScoreReportRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_SELECT_ENDPOINT;
import static com.yolt.creditscoring.service.user.model.InvitationStatus.ACCOUNT_SELECTED;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CreditScoreUserAccountControllerIT {

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private CreditScoreReportRepository creditScoreRepository;

    @AfterEach
    void afterTest() {
        creditScoreRepository.deleteAll();
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldReturnAcceptWhenFetchAccounts() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setInvitationHash(SOME_USER_HASH)
                .setYoltUserSiteId(UUID.fromString("4029e51e-451f-493d-8825-25e566ff4613"))
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(USER_ACCOUNTS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldFetchAccounts() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setInvitationHash(SOME_USER_HASH)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(USER_ACCOUNTS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", equalTo("109d740d-2932-4916-a62d-22e363e34dc1")))
                .andExpect(jsonPath("$.[0].balance", equalTo(652.29)))
                .andExpect(jsonPath("$.[0].currency", equalTo("EUR")))
                .andExpect(jsonPath("$.[0].accountNumber", equalTo("NL05INGB1234567890")))
                .andExpect(jsonPath("$.[1].id", equalTo("bc619bf9-d6f1-4f36-89b6-94af20dc0817")))
                .andExpect(jsonPath("$.[1].balance", equalTo(59186.01)))
                .andExpect(jsonPath("$.[1].currency", equalTo("EUR")))
                .andExpect(jsonPath("$.[1].accountNumber", equalTo("NL33INGB1234567890")))
                .andExpect(jsonPath("$.[2].id", equalTo("eec331b3-ec4b-44e9-8749-209c111b0d82")))
                .andExpect(jsonPath("$.[2].balance", equalTo(0.00)))
                .andExpect(jsonPath("$.[2].currency", equalTo("EUR")))
                .andExpect(jsonPath("$.[2].accountNumber", equalTo("NL75INGB1234567890")));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForFetchAccounts(InvitationStatus invitationStatus) throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(invitationStatus)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setInvitationHash(SOME_USER_HASH)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(USER_ACCOUNTS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldSelectAccount() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(USER_ACCOUNTS_SELECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id":"109d740d-2932-4916-a62d-22e363e34dc1"}
                        """));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isAccepted());

        then(creditScoreUserRepository.findById(SOME_USER_ID)).get()
                .hasFieldOrPropertyWithValue("selectedAccountId", UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .hasFieldOrPropertyWithValue("status", ACCOUNT_SELECTED);
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForSelectAccount(InvitationStatus invitationStatus) throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(invitationStatus)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(post(USER_ACCOUNTS_SELECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id":"109d740d-2932-4916-a62d-22e363e34dc1"}
                        """));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldNoUserBeAbleToSelectAccountWhenAlreadySelected() throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setSelectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(USER_ACCOUNTS_SELECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id":"109d740d-2932-4916-a62d-22e363e34dc1"}
                        """));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorCode", notNullValue()))
                .andExpect(jsonPath("$.errorType", equalTo("FLOW_ENDED")));
    }

}
