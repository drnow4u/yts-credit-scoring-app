package com.yolt.creditscoring.controller.admin;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_USER_CFA_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class AdminControllerIntegrationTest {

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void when_theUserIsLoggedInAsClientAdmin_then_IExpectA200() throws Exception {
        // When
        ResultActions perform = mvc.perform(get("/api/admin/me")
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(OAUTH_ADMIN_USER_CLIENT_ADMIN.getEmail())))
                .andExpect(jsonPath("$.idpId", is(OAUTH_ADMIN_USER_CLIENT_ADMIN.getIdpId())))
                .andExpect(jsonPath("$.roles", is(List.of("ROLE_CLIENT_ADMIN"))))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void when_theUserIsLoggedInAsCFAAdmin_then_IExpectA200() throws Exception {
        // When
        ResultActions perform = mvc.perform(get("/api/admin/me")
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_USER_CFA_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(OAUTH_USER_CFA_ADMIN.getEmail())))
                .andExpect(jsonPath("$.idpId", is(OAUTH_USER_CFA_ADMIN.getIdpId())))
                .andExpect(jsonPath("$.roles", is(List.of("ROLE_CFA_ADMIN"))))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }
}