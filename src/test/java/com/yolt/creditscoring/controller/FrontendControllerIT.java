package com.yolt.creditscoring.controller;

import com.yolt.creditscoring.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class FrontendControllerIT {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/login", "/consent"})
    void isSinglePageApplicationSetup(String requestPath) throws Exception {
        // When
        ResultActions perform = mvc.perform(get(requestPath));

        // Then
        SecurityHelper.hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());
    }

}
