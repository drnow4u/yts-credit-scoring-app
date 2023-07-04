package com.yolt.creditscoring.controller.user.legaldocument;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.legaldocument.model.LegalDocumentRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.PRIVACY_POLICY_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.TERMS_CONDITIONS_ENDPOINT;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class LegalDocumentControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @BeforeEach
    void setUp() {
        creditScoreUserRepository.save(new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL));
    }

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
    }

    @Test
    void getTermsAndConditions() throws Exception {
        // When
        ResultActions perform = mvc.perform(get(TERMS_CONDITIONS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.html", containsString("Yolt AIS / PIS Terms and Conditions")));
    }

    @Test
    void getPrivacyPolicy() throws Exception {
        // When
        ResultActions perform = mvc.perform(get(PRIVACY_POLICY_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.html", containsString("YOLT PRIVACY POLICY")));
    }
}
