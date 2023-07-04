package com.yolt.creditscoring.controller.user.site;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
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

import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.site.SiteController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class SiteControllerIT {

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private MockMvc mvc;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldListSites() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(get(SITES_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", equalTo("497f6eca-6276-4993-bfeb-53cbbbba6f08")))
                .andExpect(jsonPath("$.[0].name", equalTo("Credit Agricole")))
                .andExpect(jsonPath("$.[1].id", equalTo("a2d82da0-9bea-4f55-bd4e-88bd5e919d76")))
                .andExpect(jsonPath("$.[1].name", equalTo("ABM Amro")))
                .andExpect(jsonPath("$.[2].id", equalTo("1e1d7cd1-3652-41ea-8dd3-44c868a7aa75")))
                .andExpect(jsonPath("$.[2].name", equalTo("Barclays")))
                .andExpect(jsonPath("$.[3].id", equalTo("eefed7b0-8e5c-4941-8b30-2f9af7bb6060")))
                .andExpect(jsonPath("$.[3].name", equalTo("HSBC")));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnForbiddenForListSites(InvitationStatus invitationStatus) throws Exception {
        // Given
        preprareUser(invitationStatus);

        // When
        ResultActions perform = mvc.perform(get(SITES_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldConnectUserAndSite() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(post(SITES_CONNECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"siteId\": \"%s\"}", SOME_YOLT_SITE_ID)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl", equalTo("https://yoltbank.sandbox.yolt.io/yoltbank/yolt-test-bank/authorize?redirect_uri=https://yts-credit-scoring-app:8080/yts-credit-scoring-app/callback&state=c0e3d239-f9c7-4de3-8fab-77c0d5042b40")));

        assertThat(creditScoreUserRepository.findByInvitationHash(SOME_USER_HASH))
                .isPresent()
                .map(CreditScoreUser::getYoltUserSiteId)
                .get()
                .isEqualTo(UUID.fromString("061ae378-f773-4625-9796-06e72e1e5a86"));

        assertThat(userJourneyRepository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForConnectUserAndSite(InvitationStatus invitationStatus) throws Exception {
        // Given
        preprareUser(invitationStatus);

        // When
        ResultActions perform = mvc.perform(post(SITES_CONNECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"siteId\": \"%s\"}", SOME_YOLT_SITE_ID)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldReturnBadRequestWhenUserIsAlreadyConnectedToSite() throws Exception {
        //Given
        preprareUser(InvitationStatus.INVITED);

        CreditScoreUser userWithConnectedSite = new CreditScoreUser()
                .setId(SOME_USER_ID_2)
                .setName(SOME_USER_NAME_2)
                .setEmail(SOME_USER_EMAIL_2)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH_2)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(userWithConnectedSite);

        // When
        ResultActions perform = mvc.perform(post(SITES_CONNECT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH_2))
                .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"siteId\": \"%s\"}", SOME_YOLT_SITE_ID)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("BANK_CONNECTION_EXIST")));

        assertThat(userJourneyRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateUserAndSiteWithXRealIpHeader() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId", equalTo("ffa198a7-dc92-4aad-9237-95c0a045091c")));

        assertThat(creditScoreUserRepository.findByInvitationHash(SOME_USER_HASH))
                .isPresent()
                .map(CreditScoreUser::getYoltActivityId)
                .get()
                .isEqualTo(UUID.fromString("ffa198a7-dc92-4aad-9237-95c0a045091c"));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.BANK_CONSENT_ACCEPTED));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnForbiddenForCreateUserAndSiteWithXRealIpHeader(InvitationStatus invitationStatus) throws Exception {
        // Given
        preprareUser(invitationStatus);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldCreateUserAndSiteWithRemoteAddrHeader() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("REMOTE_ADDR", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId", equalTo("ffa198a7-dc92-4aad-9237-95c0a045091c")));

        assertThat(creditScoreUserRepository.findByInvitationHash(SOME_USER_HASH))
                .isPresent()
                .map(CreditScoreUser::getYoltActivityId)
                .get()
                .isEqualTo(UUID.fromString("ffa198a7-dc92-4aad-9237-95c0a045091c"));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.BANK_CONSENT_ACCEPTED));

    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnForbiddenForCreateUserAndSiteWithRemoteAddrHeader(InvitationStatus invitationStatus) throws Exception {
        // Given
        preprareUser(invitationStatus);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("REMOTE_ADDR", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));

    }

    @Test
    void shouldHandleMultiStepBankFlow() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("REMOTE_ADDR", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL_MULTI_STEP)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl", equalTo("https://yoltbank.sandbox.yolt.io/yoltbank/yolt-test-bank/authorize/second-consent?redirect_uri=http://localhost:8080/yts-credit-scoring-app/site-connect-callback&state=ff739ea7-c727-47f5-965e-03647368a496&code=eyJhbGciOiJub25lIn0.eyJleHAiOjE2MjM2Njc4OTAsInN1YiI6IkxJR0hUIiwiaWF0IjoxNjE1ODkxODkwfQ")));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.BANK_CONSENT_ACCEPTED));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForHandleMultiStepBankFlow(InvitationStatus invitationStatus) throws Exception {
        // Given
        preprareUser(invitationStatus);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("REMOTE_ADDR", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL_MULTI_STEP)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldReturnBadRequestWhenConnectionToBankFailed() throws Exception {
        // Given
        preprareUser(InvitationStatus.INVITED);

        // When
        ResultActions perform = mvc.perform(post(USER_SITE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .header("REMOTE_ADDR", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL_ERROR)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("BANK_CONSENT_REFUSED")));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.BANK_CONSENT_REFUSED));
    }

    private void preprareUser(InvitationStatus invitationStatus) {
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
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);
    }
}
