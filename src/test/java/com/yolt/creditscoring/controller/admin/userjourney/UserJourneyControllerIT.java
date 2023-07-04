package com.yolt.creditscoring.controller.admin.userjourney;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ID;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.userjourney.UserJourneyController.ADMIN_METRICS_ENDPOINT;
import static com.yolt.creditscoring.controller.admin.userjourney.UserJourneyController.ADMIN_METRICS_YEARS_ENDPOINT;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UserJourneyControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private JwtCreationService jwtCreationService;

    @AfterEach
    void afterTest() {
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldFetchMetricsForClient() throws Exception {
        // Given
        final int currentYear = LocalDate.now().getYear();
        prepareUserJourneyMetricForTestPurposes(currentYear);

        // When
        ResultActions perform = mvc.perform(get(ADMIN_METRICS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].year", is(currentYear)))
                .andExpect(jsonPath("$.[0].month", is(1)))
                .andExpect(jsonPath("$.[0].status", is("CONSENT_ACCEPTED")))
                .andExpect(jsonPath("$.[0].count", is(2)))
                .andExpect(jsonPath("$.[1].year", is(currentYear)))
                .andExpect(jsonPath("$.[1].month", is(1)))
                .andExpect(jsonPath("$.[1].status", is("INVITED")))
                .andExpect(jsonPath("$.[1].count", is(1)));
    }

    @Test
    void shouldFetchAllAvailableYearsForClients() throws Exception {
        // Given
        final int currentYear = LocalDate.now().getYear();
        prepareUserJourneyMetricForTestPurposes(currentYear);

        // When
        ResultActions perform = mvc.perform(get(ADMIN_METRICS_YEARS_ENDPOINT)
                .header(org.springframework.http.HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]", is(currentYear - 1)))
                .andExpect(jsonPath("$.[1]", is(currentYear)));
    }

    private void prepareUserJourneyMetricForTestPurposes(int currentYear) {
        userJourneyRepository.saveAll(Arrays.asList(
                new UserJourneyMetric(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00").withYear(currentYear),
                        SOME_CLIENT_ID,
                        JourneyStatus.CONSENT_ACCEPTED
                ),
                new UserJourneyMetric(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00").withYear(currentYear),
                        SOME_CLIENT_ID,
                        JourneyStatus.INVITED
                ),
                new UserJourneyMetric(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2020-01-01T09:50:03.619675+00:00").withYear(currentYear - 1),
                        SOME_CLIENT_ID,
                        JourneyStatus.CONSENT_ACCEPTED
                ),
                new UserJourneyMetric(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2021-01-02T09:50:03.619675+00:00").withYear(currentYear),
                        SOME_CLIENT_ID,
                        JourneyStatus.CONSENT_ACCEPTED
                )
        ));
    }
}
