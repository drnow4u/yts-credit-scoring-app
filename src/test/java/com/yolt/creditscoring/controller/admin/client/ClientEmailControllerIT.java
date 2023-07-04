package com.yolt.creditscoring.controller.admin.client;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.client.ClientEmailController.CLIENT_TEMPLATE_ENDPOINT;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ClientEmailControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Test
    void getAccount() throws Exception {
        // When
        ResultActions result = mvc.perform(get(CLIENT_TEMPLATE_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is("0f3602a8-7e22-4f14-b339-e3c52badc163")))
                .andExpect(jsonPath("$.[0].template", is("UserInvitation_Test_Client")));
    }
}
