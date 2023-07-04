package com.yolt.creditscoring.service.userjourney.reporting.userjourney;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.creditscore.LocalDateConverter;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import com.yolt.creditscoring.service.userjourney.reporting.ClientReport;
import com.yolt.creditscoring.service.userjourney.reporting.UserJourneyMonthReport;
import com.yolt.creditscoring.service.userjourney.reporting.UserJourneyReportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.BDDAssertions.then;

@IntegrationTest
class UserJourneyReportServiceIT {

    @Autowired
    UserJourneyRepository userJourneyRepository;

    @Autowired
    UserJourneyReportService userJourneyReportService;

    @AfterEach
    void afterTest() {
        userJourneyRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "2021-01-02", "2021-01-20", "2021-01-31", "2021-02-01"
    })
    void shouldGenerateMonthlyReport(@ConvertWith(LocalDateConverter.class) LocalDate reportDate) {
        // Given
        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.parse("2021-01-01T08:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID);
        userJourneyMetric2.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(reportDate, ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2021-01-01T00:00+01:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2021-01-31T23:59:59.999999999+01:00"));
        then(userJourneyMonthReport.getReports()).usingFieldByFieldElementComparator()
                .containsOnly(
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.INVITED)
                                .count(1)
                                .build(),
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.CONSENT_ACCEPTED)
                                .count(1)
                                .build());
    }

    @Test
    void shouldGenerateEmptyMonthlyReportForNoDataInSelectedMonth() {
        // Given
        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.parse("2021-01-01T08:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID);
        userJourneyMetric2.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(LocalDate.of(2021, 2, 20), ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2021-02-01T00:00+01:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2021-02-28T23:59:59.999999999+01:00"));
        then(userJourneyMonthReport.getReports()).isEmpty();
    }

    @Test
    void shouldGenerateMonthlyReportForTwoUsersSameClient() {
        // Given
        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.parse("2021-01-01T08:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID_2);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID);
        userJourneyMetric2.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(LocalDate.of(2021, 1, 20), ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2021-01-01T00:00+01:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2021-01-31T23:59:59.999999999+01:00"));
        then(userJourneyMonthReport.getReports()).usingFieldByFieldElementComparator()
                .containsOnly(
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.INVITED)
                                .count(1)
                                .build(),
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.CONSENT_ACCEPTED)
                                .count(1)
                                .build());
    }

    @Test
    void shouldGenerateMonthlyReportForTwoClient() {
        // Given
        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.parse("2021-01-01T08:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID_2);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID_2);
        userJourneyMetric2.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2021-01-01T09:50:03.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(LocalDate.of(2021, 1, 20), ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2021-01-01T00:00+01:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2021-01-31T23:59:59.999999999+01:00"));
        then(userJourneyMonthReport.getReports()).usingFieldByFieldElementComparator()
                .containsOnly(
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.INVITED)
                                .count(1)
                                .build(),
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_2_NAME)
                                .status(JourneyStatus.CONSENT_ACCEPTED)
                                .count(1)
                                .build());
    }

    @Test
    void shouldGenerateMonthlyReportCornerCasesWinterTime() {
        // Given
        UserJourneyMetric userJourneyMetric1 = new UserJourneyMetric();
        userJourneyMetric1.setId(UUID.randomUUID());
        userJourneyMetric1.setUserId(SOME_USER_ID);
        userJourneyMetric1.setClientId(SOME_CLIENT_ID_2);
        userJourneyMetric1.setStatus(JourneyStatus.INVITED);
        userJourneyMetric1.setCreatedDate(OffsetDateTime.parse("2020-12-31T22:59:59.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric1);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID);
        userJourneyMetric2.setStatus(JourneyStatus.INVITED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2020-12-31T23:00:01.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        UserJourneyMetric userJourneyMetric3 = new UserJourneyMetric();
        userJourneyMetric3.setId(UUID.randomUUID());
        userJourneyMetric3.setUserId(SOME_USER_ID);
        userJourneyMetric3.setClientId(SOME_CLIENT_ID);
        userJourneyMetric3.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric3.setCreatedDate(OffsetDateTime.parse("2021-01-31T22:59:59.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric3);

        UserJourneyMetric userJourneyMetric4 = new UserJourneyMetric();
        userJourneyMetric4.setId(UUID.randomUUID());
        userJourneyMetric4.setUserId(SOME_USER_ID);
        userJourneyMetric4.setClientId(SOME_CLIENT_ID_2);
        userJourneyMetric4.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric4.setCreatedDate(OffsetDateTime.parse("2021-01-31T23:00:01.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric4);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(LocalDate.of(2021, 2, 1), ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2021-01-01T00:00+01:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2021-01-31T23:59:59.999999999+01:00"));
        then(userJourneyMonthReport.getReports()).usingFieldByFieldElementComparator()
                .containsOnly(
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.INVITED)
                                .count(1)
                                .build(),
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.CONSENT_ACCEPTED)
                                .count(1)
                                .build());
    }

    @Test
    void shouldGenerateMonthlyReportCornerCasesSummerTime() {
        // Given
        UserJourneyMetric userJourneyMetric1 = new UserJourneyMetric();
        userJourneyMetric1.setId(UUID.randomUUID());
        userJourneyMetric1.setUserId(SOME_USER_ID);
        userJourneyMetric1.setClientId(SOME_CLIENT_ID_2);
        userJourneyMetric1.setStatus(JourneyStatus.INVITED);
        userJourneyMetric1.setCreatedDate(OffsetDateTime.parse("2020-07-31T21:59:59.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric1);

        UserJourneyMetric userJourneyMetric2 = new UserJourneyMetric();
        userJourneyMetric2.setId(UUID.randomUUID());
        userJourneyMetric2.setUserId(SOME_USER_ID);
        userJourneyMetric2.setClientId(SOME_CLIENT_ID);
        userJourneyMetric2.setStatus(JourneyStatus.INVITED);
        userJourneyMetric2.setCreatedDate(OffsetDateTime.parse("2020-07-31T22:00:01.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric2);

        UserJourneyMetric userJourneyMetric3 = new UserJourneyMetric();
        userJourneyMetric3.setId(UUID.randomUUID());
        userJourneyMetric3.setUserId(SOME_USER_ID);
        userJourneyMetric3.setClientId(SOME_CLIENT_ID);
        userJourneyMetric3.setStatus(JourneyStatus.CONSENT_ACCEPTED);
        userJourneyMetric3.setCreatedDate(OffsetDateTime.parse("2020-08-31T21:59:59.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric3);

        UserJourneyMetric userJourneyMetric4 = new UserJourneyMetric();
        userJourneyMetric4.setId(UUID.randomUUID());
        userJourneyMetric4.setUserId(SOME_USER_ID);
        userJourneyMetric4.setClientId(SOME_CLIENT_ID_2);
        userJourneyMetric4.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric4.setCreatedDate(OffsetDateTime.parse("2020-08-31T22:00:01.619675+00:00"));
        userJourneyRepository.save(userJourneyMetric4);

        // When
        UserJourneyMonthReport userJourneyMonthReport = userJourneyReportService.reportForMonth(LocalDate.of(2020, 9, 1), ZoneId.of("Europe/Amsterdam"));

        // Then
        then(userJourneyMonthReport.getFrom()).isEqualTo(OffsetDateTime.parse("2020-08-01T00:00+02:00"));
        then(userJourneyMonthReport.getTill()).isEqualTo(OffsetDateTime.parse("2020-08-31T23:59:59.999999999+02:00"));
        then(userJourneyMonthReport.getReports()).usingFieldByFieldElementComparator()
                .containsOnly(
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.INVITED)
                                .count(1)
                                .build(),
                        ClientReport.builder()
                                .clientName(SOME_CLIENT_NAME)
                                .status(JourneyStatus.CONSENT_ACCEPTED)
                                .count(1)
                                .build());
    }

}
