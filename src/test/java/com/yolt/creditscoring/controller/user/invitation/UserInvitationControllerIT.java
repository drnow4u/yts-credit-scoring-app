package com.yolt.creditscoring.controller.user.invitation;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_INVITATION_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.groups.Tuple.tuple;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UserInvitationControllerIT {

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
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
    }

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldValidateUser() throws Exception {
        // When
        final ResultActions result = mvc.perform(get(USER_INVITATION_ENDPOINT, SOME_USER_HASH));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void shouldReturnBadRequestForExpiredInvitation() throws Exception {
        //Given
        CreditScoreUser userExpiredInvitation = new CreditScoreUser()
                .setId(SOME_USER_ID_2)
                .setName(SOME_USER_NAME_2)
                .setEmail(SOME_USER_EMAIL_2)
                .setDateTimeInvited(SOME_NOT_VALID_INVITATION_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH_2)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(userExpiredInvitation);

        // When
        ResultActions perform = mvc.perform(get(USER_INVITATION_ENDPOINT, SOME_USER_HASH_2));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("INVITATION_EXPIRED")));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID_2, SOME_CLIENT_ID, JourneyStatus.EXPIRED));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForInvalidInvitation(InvitationStatus initialStatus) throws Exception {
        //Given
        CreditScoreUser userExpiredInvitation = new CreditScoreUser()
                .setId(SOME_USER_ID_2)
                .setName(SOME_USER_NAME_2)
                .setEmail(SOME_USER_EMAIL_2)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(initialStatus)
                .setInvitationHash(SOME_USER_HASH_2)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(userExpiredInvitation);

        // When
        ResultActions result = mvc.perform(get(USER_INVITATION_ENDPOINT, SOME_USER_HASH_2));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("INVITATION_EXPIRED")));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class)
    void shouldReturnBadRequestForExpiredInvitation(InvitationStatus initialStatus) throws Exception {
        //Given
        CreditScoreUser userExpiredInvitation = new CreditScoreUser()
                .setId(SOME_USER_ID_2)
                .setName(SOME_USER_NAME_2)
                .setEmail(SOME_USER_EMAIL_2)
                .setDateTimeInvited(SOME_NOT_VALID_INVITATION_DATE)
                .setStatus(initialStatus)
                .setInvitationHash(SOME_USER_HASH_2)
                .setClientId(SOME_CLIENT_ID_2)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(userExpiredInvitation);

        // When
        ResultActions result = mvc.perform(get(USER_INVITATION_ENDPOINT, SOME_USER_HASH_2));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("INVITATION_EXPIRED")));
    }

    @Test
    void shouldSaveUserConsent() throws Exception {
        // When
        final ResultActions result = mvc.perform(post(USER_CONSENT_ENDPOINT)
                .header("user-agent", SOME_USER_AGENT)
                .header("x-real-ip", SOME_USER_IP)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                          {"consent": "true"}
                        """));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk());

        List<CreditScoreUser> users = creditScoreUserRepository.findAllByClientId(SOME_CLIENT_ID);
        assertThat(users).hasSize(1);
        CreditScoreUser savedUser = users.get(0);
        assertThat(savedUser.getId()).isEqualTo(SOME_USER_ID);
        assertThat(savedUser.getName()).isEqualTo(SOME_USER_NAME);
        assertThat(savedUser.getEmail()).isEqualTo(SOME_USER_EMAIL);
        assertThat(savedUser.getStatus()).isEqualTo(InvitationStatus.INVITED);
        assertThat(savedUser.getInvitationHash()).isEqualTo(SOME_USER_HASH);
        assertThat(savedUser.getClientId()).isEqualTo(SOME_CLIENT_ID);
        assertThat(savedUser.getPrivacyPolicyId()).isEqualTo(UUID.fromString("ea8d396a-be90-4b38-9e17-dac08d0b25ab"));
        assertThat(savedUser.getTermsAndConditionId()).isEqualTo(UUID.fromString("9da7079d-7896-413c-b847-a51fc17fc434"));
        assertThat(savedUser.isConsent()).isTrue();
        assertThat(savedUser.getDateTimeConsent()).isNotNull();
        assertThat(savedUser.getIpAddress()).isEqualTo(SOME_USER_IP);
        assertThat(savedUser.getUserAgent()).isEqualTo(SOME_USER_AGENT);
        assertThat(savedUser.getYoltUserId()).isEqualTo(SOME_YOLT_USER_ID);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.CONSENT_ACCEPTED));
    }

    @Test
    void shouldSaveUserRejectConsent() throws Exception {
        // When
        final ResultActions result = mvc.perform(post(USER_CONSENT_ENDPOINT)
                .header("user-agent", SOME_USER_AGENT)
                .header("x-real-ip", SOME_USER_IP)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                          {"consent": "false"}
                        """));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk());

        List<CreditScoreUser> users = creditScoreUserRepository.findAllByClientId(SOME_CLIENT_ID);
        assertThat(users).hasSize(1);
        CreditScoreUser savedUser = users.get(0);
        assertThat(savedUser.getId()).isEqualTo(SOME_USER_ID);
        assertThat(savedUser.getName()).isEqualTo(SOME_USER_NAME);
        assertThat(savedUser.getEmail()).isEqualTo(SOME_USER_EMAIL);
        assertThat(savedUser.getStatus()).isEqualTo(InvitationStatus.REFUSED);
        assertThat(savedUser.getInvitationHash()).isEqualTo(SOME_USER_HASH);
        assertThat(savedUser.getClientId()).isEqualTo(SOME_CLIENT_ID);
        assertThat(savedUser.getTermsAndConditionId()).isNull();
        assertThat(savedUser.isConsent()).isFalse();
        assertThat(savedUser.getDateTimeConsent()).isNull();
        assertThat(savedUser.getIpAddress()).isNull();
        assertThat(savedUser.getUserAgent()).isNull();
        assertThat(savedUser.getYoltUserId()).isNull();

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.CONSENT_REFUSED));
    }
}
