package com.yolt.creditscoring.controller.admin.account;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.account.ClientAdminController.ACCOUNT_ENDPOINT;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class ClientAdminControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Test
    void getAccount() throws Exception {

        // When
        ResultActions perform = mvc.perform(get(ACCOUNT_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("adminuser@test.com")))
                .andExpect(jsonPath("$.name", is("Some Client")))
                .andExpect(jsonPath("$.clientSettings.defaultLanguage", is("NL")))
                .andExpect(jsonPath("$.clientSettings.signatureVerificationFeatureToggle", is(true)))
                .andExpect(jsonPath("$.clientSettings.categoryFeatureToggle", is(true)))
                .andExpect(jsonPath("$.clientSettings.monthsFeatureToggle", is(true)))
                .andExpect(jsonPath("$.clientSettings.overviewFeatureToggle", is(true)))
                .andExpect(jsonPath("$.clientSettings.pdscoreFeatureToggle", is(true)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void given_noAuthorizationHeader_then_theAPIShouldReturnUnauthorized() throws Exception {
        // When
        ResultActions perform = mvc.perform(get(ACCOUNT_ENDPOINT));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isUnauthorized());

    }
}
