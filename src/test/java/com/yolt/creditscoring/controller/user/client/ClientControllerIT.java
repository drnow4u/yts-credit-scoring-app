package com.yolt.creditscoring.controller.user.client;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.client.ClientController.CLIENT_ENDPOINT;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ClientControllerIT {

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

    @Test
    void getClientName() throws Exception {
        // Given
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

        // When
        ResultActions perform = mvc.perform(get(CLIENT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));
        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo(SOME_CLIENT_NAME)))
                .andExpect(jsonPath("$.logo", notNullValue()))
                .andExpect(jsonPath("$.language", equalTo(SOME_CLIENT_LANGUAGE)))
                .andExpect(jsonPath("$.additionalTextConsent", equalTo(SOME_CLIENT_ADDITIONAL_TEXT)));
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED", "REFUSED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestClientName(InvitationStatus invitationStatus) throws Exception {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(invitationStatus)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(CLIENT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));
        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }
}