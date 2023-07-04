package com.yolt.creditscoring.configuration.security.user;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_SELECT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.client.ClientController.CLIENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.creditscore.CreditScoreReportController.CASHFLOW_OVERVIEW_FOR_USER;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.PRIVACY_POLICY_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.TERMS_CONDITIONS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.site.SiteController.SITES_CONNECT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.site.SiteController.USER_SITE_ENDPOINT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UserFlowVerificationFilterIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("endpointsThatShouldBeBlockForGivenInvitationStatus")
    void shouldBlockGivenEndpointsWhenInvitationStatusIsNotValid(String endpoint, HttpMethod httpMethod, InvitationStatus status) throws Exception {
        // Given
        creditScoreUserRepository.save(createCreditScoreUserWithGivenStatus(status));

        // When
        if (httpMethod.equals(HttpMethod.GET)) {
            mvc.perform(get(endpoint)
                            .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)))
                    .andExpect(status().isBadRequest());
        }

        if (httpMethod.equals(HttpMethod.POST)) {
            mvc.perform(post(endpoint)
                            .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)))
                    .andExpect(status().isBadRequest());
        }
    }

    private static Stream<Arguments> endpointsThatShouldBeBlockForGivenInvitationStatus() {

        List<Arguments> arguments = new ArrayList<>();

        List<InvitationStatus> invitationStatuses = Arrays.asList(
                InvitationStatus.EXPIRED,
                InvitationStatus.COMPLETED,
                InvitationStatus.REFUSED,
                InvitationStatus.REPORT_SHARING_REFUSED,
                InvitationStatus.ERROR_BANK,
                InvitationStatus.REFUSED_BANK_CONSENT);


        for (InvitationStatus status : invitationStatuses) {
            arguments.add(Arguments.of(CASHFLOW_OVERVIEW_FOR_USER, HttpMethod.GET, status));
            arguments.add(Arguments.of(USER_ACCOUNTS_ENDPOINT, HttpMethod.GET, status));
            arguments.add(Arguments.of(USER_ACCOUNTS_SELECT_ENDPOINT, HttpMethod.POST, status));
            arguments.add(Arguments.of(SITES_CONNECT_ENDPOINT, HttpMethod.POST, status));
            arguments.add(Arguments.of(CASHFLOW_OVERVIEW_FOR_USER, HttpMethod.GET, status));
            arguments.add(Arguments.of(USER_SITE_ENDPOINT, HttpMethod.POST, status));
        }

        List<InvitationStatus> invitationStatusesWithoutRefusedStatus = Arrays.asList(
                InvitationStatus.EXPIRED,
                InvitationStatus.COMPLETED,
                InvitationStatus.REPORT_SHARING_REFUSED,
                InvitationStatus.ERROR_BANK,
                InvitationStatus.REFUSED_BANK_CONSENT);

        for (InvitationStatus status : invitationStatusesWithoutRefusedStatus) {
            arguments.add(Arguments.of(USER_CONSENT_ENDPOINT, HttpMethod.POST, status));
            arguments.add(Arguments.of(TERMS_CONDITIONS_ENDPOINT, HttpMethod.GET, status));
            arguments.add(Arguments.of(PRIVACY_POLICY_ENDPOINT, HttpMethod.GET, status));
            arguments.add(Arguments.of(CLIENT_ENDPOINT, HttpMethod.GET, status));
        }

        return arguments.stream();
    }

    @Test
    void shouldAllowGivenEndpointsWhenInvitationStatusIsRefused() throws Exception {
        // Given
        creditScoreUserRepository.save(createCreditScoreUserWithGivenStatus(InvitationStatus.REFUSED));

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        mvc.perform(get(TERMS_CONDITIONS_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)))
                .andExpect(status().isOk());

        mvc.perform(get(PRIVACY_POLICY_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)))
                .andExpect(status().isOk());

        mvc.perform(get(CLIENT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)))
                .andExpect(status().isOk());

        mvc.perform(post(USER_CONSENT_ENDPOINT)
                        .header("user-agent", SOME_USER_AGENT)
                        .header("x-real-ip", SOME_USER_IP)
                        .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consent\": \"true\" }"))
                .andExpect(status().isOk());
    }

    private static CreditScoreUser createCreditScoreUserWithGivenStatus(InvitationStatus invitationStatus) {
        return new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(invitationStatus)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);
    }
}
