package com.yolt.creditscoring.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController;
import com.yolt.creditscoring.controller.customer.CustomerAPIUserInvitationDTO;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController.CREATE_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.controller.customer.CustomerAPIController.INVITE_USER_CLIENT_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.controller.customer.CustomerCreditReportController.FETCH_USER_REPORT_V1_ENDPOINT;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.CLIENT_TOKEN_SUBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CustomerTokenFlowIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private ClientTokenRepository clientTokenRepository;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
        clientTokenRepository.deleteAll();
    }

    @Test
    void shouldPassClientTokenFlow() throws Exception {
        // When
        ResultActions perform = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "JWT",
                                "permissions": ["INVITE_USER", "DOWNLOAD_REPORT"]
                            }
                        """));

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientToken", notNullValue()));

        Iterable<ClientTokenEntity> result = clientTokenRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result).extracting("clientId", "status")
                .contains(tuple(SOME_CLIENT_ID, ClientTokenStatus.ACTIVE));
        assertThat(result).flatExtracting("permissions")
                .contains(ClientTokenPermission.INVITE_USER);

        // Then - verify token claims
        ObjectMapper mapper = new ObjectMapper();
        String contentAsString = perform.andReturn().getResponse().getContentAsString();
        ClientTokenController.ClientTokenResponse clientTokenResponse =
                mapper.readValue(contentAsString, ClientTokenController.ClientTokenResponse.class);

        JwtClaims claimsOfClientToken = jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation(clientTokenResponse.clientToken());

        assertThat(claimsOfClientToken.getSubject()).isEqualTo(CLIENT_TOKEN_SUBJECT);
        assertThat(claimsOfClientToken.getClaimValue("scope")).isEqualTo(List.of(
                ClientTokenPermission.INVITE_USER.name(),
                ClientTokenPermission.DOWNLOAD_REPORT.name()));


        // When - perform second identical call
        ResultActions secondPerform = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                    {
                                        "name": "%s",
                                        "permissions": ["%s"]
                                    }
                                """, SOME_CLIENT_JWT_NAME, ClientTokenPermission.INVITE_USER)
                )
        );

        // Then - client token should be different from the first one
        secondPerform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientToken", notNullValue()));


        String secondContentAsString = secondPerform.andReturn().getResponse().getContentAsString();

        ClientTokenController.ClientTokenResponse secondClientTokenResponse =
                mapper.readValue(secondContentAsString, ClientTokenController.ClientTokenResponse.class);

        assertThat(secondClientTokenResponse.clientToken()).isNotEqualTo(clientTokenResponse.clientToken());

        // Then - client token should not have access to other admin and user endpoint
        mvc.perform(get("/api/admin/account")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenResponse.clientToken()))
                .andExpect(status().isForbidden());

        mvc.perform(post(USER_CONSENT_ENDPOINT)
                .header("user-agent", SOME_USER_AGENT)
                .header("x-real-ip", SOME_USER_IP)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenResponse.clientToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"consent\": \"true\" }"))
                .andExpect(status().isUnauthorized());

        // Then - should invite user with client token (with client email ID and without it)
        mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenResponse.clientToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()));

        var inviteUserResponse = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenResponse.clientToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "clientEmailId": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andReturn()
                .getResponse();

        var inviteUserDTO =
                mapper.readValue(inviteUserResponse.getContentAsString(), CustomerAPIUserInvitationDTO.class);

        Iterable<CreditScoreUser> users = creditScoreUserRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting("name", "email")
                .contains(
                        tuple(SOME_USER_NAME, SOME_USER_EMAIL),
                        tuple(SOME_USER_NAME, SOME_USER_EMAIL)
                );

        // When - should not fetch non-existing credit report
        ResultActions performReport = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, inviteUserDTO.userId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenResponse.clientToken()));

        // Then
        performReport
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInvitationStatus", is("INVITED")));
    }
}
