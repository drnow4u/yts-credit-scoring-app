package com.yolt.creditscoring.controller.user.creditscore;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.creditscore.model.*;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportEntity;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.user.creditscore.CreditScoreReportController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@TestPropertySource(properties = {
        "yolt.creditScoreExecutor.async=false",})
class CreditScoreReportControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private CreditScoreReportRepository creditScoreReportRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private RecurringTransactionsMonthlyReportRepository recurringTransactionsMonthlyReportRepository;

    @SpyBean
    VaultSecretKeyService vaultSecretKeyService;

    private Appender<ILoggingEvent> logAppender;
    private ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

    @BeforeEach
    void setUp() {
        WireMock.resetAllRequests();
        userJourneyRepository.deleteAll();

        logAppender = mock(Appender.class);
        captorLoggingEvent = ArgumentCaptor.forClass(ILoggingEvent.class);
        final Logger logger = (Logger) LoggerFactory.getLogger(SemaEventLogger.class);
        logger.setLevel(Level.ALL);
        logger.addAppender(logAppender);
    }

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldUserWithSelectedAccountGetCreditScoreReport() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(get(CASHFLOW_OVERVIEW_FOR_USER)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additionalTextReport", equalTo(SOME_CLIENT_ADDITIONAL_TEXT)))
                .andExpect(jsonPath("$.report.accountHolder", equalTo("Account Holder")))
                .andExpect(jsonPath("$.report.creditLimit", equalTo("-5000.05")))
                .andExpect(jsonPath("$.report.currency", equalTo("EUR")))
                .andExpect(jsonPath("$.report.bban", equalTo("05INGB1234567890")))
                .andExpect(jsonPath("$.report.iban", equalTo("NL05INGB1234567890")))
                .andExpect(jsonPath("$.report.initialBalance", equalTo("652.29")))
                .andExpect(jsonPath("$.report.lastDataFetchTime", equalTo("2020-12-21T08:57:52Z")))
                .andExpect(jsonPath("$.report.newestTransactionDate", equalTo("2020-12-21")))
                .andExpect(jsonPath("$.report.oldestTransactionDate", equalTo("2019-06-21")))
                .andExpect(jsonPath("$.report.maskedPan", equalTo("1234 **** **** 5678")))
                .andExpect(jsonPath("$.report.sortCodeAccountNumber", equalTo("1304798728")))
                .andExpect(jsonPath("$.userEmail", equalTo(SOME_USER_EMAIL)));

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(logAppender, never()).doAppend(captorLoggingEvent.capture());
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"ACCOUNT_SELECTED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnBadRequestForGetCreditScoreReport(InvitationStatus invitationStatus) throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(invitationStatus)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUserRepository.save(user);

        // When
        ResultActions perform = mvc.perform(get(CASHFLOW_OVERVIEW_FOR_USER)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH)));

        // Then
        perform.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType", is("FLOW_ENDED")));
    }

    @Test
    void shouldUserWithAlreadySelectedAccountToConfirmCreditScoreReport() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID);
        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_CONFIRM)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        CreditScoreReport report = creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID).orElseThrow();
        then(report)
                .hasFieldOrPropertyWithValue("accountReference.iban", "NL05INGB1234567890")
                .hasFieldOrPropertyWithValue("accountReference.bban", "05INGB1234567890")
                .hasFieldOrPropertyWithValue("accountReference.sortCodeAccountNumber", "1304798728")
                .hasFieldOrPropertyWithValue("accountReference.maskedPan", "1234 **** **** 5678")
                .hasFieldOrPropertyWithValue("currency", "EUR")
                .hasFieldOrPropertyWithValue("initialBalance", BigDecimal.valueOf(652.29))
                .hasFieldOrPropertyWithValue("transactionsSize", 44)
                .hasFieldOrPropertyWithValue("newestTransactionDate", LocalDate.parse("2020-12-20"))
                .hasFieldOrPropertyWithValue("oldestTransactionDate", LocalDate.parse("2020-11-21"))
                .hasFieldOrPropertyWithValue("signatureKeyId", SOME_REPORT_SIGNATURE_KEY_ID)
                .hasFieldOrPropertyWithValue("accountHolder", "Account Holder");

        then(report.getCreditScoreMonthly())
                .extracting("month", "year", "highestBalance", "lowestBalance", "averageBalance", "incomingTransactionsSize", "outgoingTransactionsSize")
                .containsExactlyInAnyOrder(
                        tuple(11, 2020, BigDecimal.valueOf(5489.03), BigDecimal.valueOf(3797.78), BigDecimal.valueOf(4702.36), 0, 15),
                        tuple(12, 2020, BigDecimal.valueOf(3797.78), BigDecimal.valueOf(652.29), BigDecimal.valueOf(2213.42), 0, 29)
                );

        var months = report.getCreditScoreMonthly().toArray(new CreditScoreMonthlyReport[]{});
        Arrays.sort(months, Comparator.comparingInt(CreditScoreMonthlyReport::getYear)
                .thenComparing(CreditScoreMonthlyReport::getMonth));
        assertThat(months[0].getCategorizedAmounts().values())
                .extracting("category", "amount")
                .containsOnly(
                        tuple(Category.EQUITY_WITHDRAWAL, BigDecimal.valueOf(266.81)),
                        tuple(Category.CORPORATE_INCOME_TAX, BigDecimal.valueOf(326.86)),
                        tuple(Category.UNSPECIFIED_TAX, new BigDecimal("180.50")),
                        tuple(Category.OTHER_EXPENSES, BigDecimal.valueOf(765.07)),
                        tuple(Category.INTEREST_AND_REPAYMENTS, BigDecimal.valueOf(152.01))
                );

        assertThat(months[1].getCategorizedAmounts().values())
                .extracting("category", "amount")
                .containsOnly(
                        tuple(Category.RENT_AND_FACILITIES, BigDecimal.valueOf(132.17)),
                        tuple(Category.TRAVEL_EXPENSES, BigDecimal.valueOf(280.34)),
                        tuple(Category.FOOD_AND_DRINKS, BigDecimal.valueOf(433.85)),
                        tuple(Category.OTHER_EXPENSES, BigDecimal.valueOf(184.63)),
                        tuple(Category.PENSION_PAYMENTS, new BigDecimal("524.90")),
                        tuple(Category.INTEREST_AND_REPAYMENTS, BigDecimal.valueOf(665.71)),
                        tuple(Category.EQUITY_WITHDRAWAL, BigDecimal.valueOf(95.22)),
                        tuple(Category.COLLECTION_COSTS, BigDecimal.valueOf(828.67))
                );

        assertThat(recurringTransactionsMonthlyReportRepository.findAllByCreditScoreId(report.getId()))
                .usingElementComparatorIgnoringFields("id")
                .containsOnly(RecurringTransactionsMonthlyReportEntity.builder()
                                .creditScoreId(report.getId())
                                .year(2020)
                                .month(12)
                                .incomeRecurringAmount(new BigDecimal("0"))
                                .incomeRecurringSize(0)
                                .outcomeRecurringAmount(new BigDecimal("69.97"))
                                .outcomeRecurringSize(1)
                                .build(),
                        RecurringTransactionsMonthlyReportEntity.builder()
                                .creditScoreId(report.getId())
                                .year(2020)
                                .month(11)
                                .incomeRecurringAmount(new BigDecimal("149.17"))
                                .incomeRecurringSize(1)
                                .outcomeRecurringAmount(new BigDecimal("0"))
                                .outcomeRecurringSize(0)
                                .build());

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.COMPLETED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .contains(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_SAVED),
                        tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(1, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    @Test
    void shouldUserWithAlreadySelectedAccountToConfirmCreditScoreReportWhenSigningKeyRotate() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID);
        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        RsaJsonWebKey rsaJsonWebKeyNew = RsaJwkGenerator.generateJwk(2048);
        UUID signingKeyIdNew = UUID.fromString("6196257e-58f6-4e29-8d7f-31e00867b214");
        rsaJsonWebKeyNew.setKeyId(signingKeyIdNew.toString());
        given(vaultSecretKeyService.getReportSignKeyId()).willReturn(signingKeyIdNew);
        given(vaultSecretKeyService.getReportSignPublicKey()).willReturn(rsaJsonWebKeyNew.getRsaPublicKey());

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_CONFIRM)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        CreditScoreReport report = creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID).orElseThrow();
        then(report)
                .hasFieldOrPropertyWithValue("accountReference.iban", "NL05INGB1234567890")
                .hasFieldOrPropertyWithValue("accountReference.bban", "05INGB1234567890")
                .hasFieldOrPropertyWithValue("accountReference.sortCodeAccountNumber", "1304798728")
                .hasFieldOrPropertyWithValue("accountReference.maskedPan", "1234 **** **** 5678")
                .hasFieldOrPropertyWithValue("currency", "EUR")
                .hasFieldOrPropertyWithValue("initialBalance", BigDecimal.valueOf(652.29))
                .hasFieldOrPropertyWithValue("transactionsSize", 44)
                .hasFieldOrPropertyWithValue("newestTransactionDate", LocalDate.parse("2020-12-20"))
                .hasFieldOrPropertyWithValue("oldestTransactionDate", LocalDate.parse("2020-11-21"))
                .hasFieldOrPropertyWithValue("signatureKeyId", signingKeyIdNew)
                .hasFieldOrPropertyWithValue("accountHolder", "Account Holder");

        then(report.getCreditScoreMonthly())
                .extracting("month", "year", "highestBalance", "lowestBalance", "averageBalance", "incomingTransactionsSize", "outgoingTransactionsSize")
                .containsExactlyInAnyOrder(
                        tuple(11, 2020, BigDecimal.valueOf(5489.03), BigDecimal.valueOf(3797.78), BigDecimal.valueOf(4702.36), 0, 15),
                        tuple(12, 2020, BigDecimal.valueOf(3797.78), BigDecimal.valueOf(652.29), BigDecimal.valueOf(2213.42), 0, 29)
                );

        var months = report.getCreditScoreMonthly().toArray(new CreditScoreMonthlyReport[]{});
        Arrays.sort(months, Comparator.comparingInt(CreditScoreMonthlyReport::getYear)
                .thenComparing(CreditScoreMonthlyReport::getMonth));
        assertThat(months[0].getCategorizedAmounts().values())
                .extracting("category", "amount")
                .containsOnly(
                        tuple(Category.EQUITY_WITHDRAWAL, BigDecimal.valueOf(266.81)),
                        tuple(Category.CORPORATE_INCOME_TAX, BigDecimal.valueOf(326.86)),
                        tuple(Category.UNSPECIFIED_TAX, new BigDecimal("180.50")),
                        tuple(Category.OTHER_EXPENSES, BigDecimal.valueOf(765.07)),
                        tuple(Category.INTEREST_AND_REPAYMENTS, BigDecimal.valueOf(152.01))
                );

        assertThat(months[1].getCategorizedAmounts().values())
                .extracting("category", "amount")
                .containsOnly(
                        tuple(Category.RENT_AND_FACILITIES, BigDecimal.valueOf(132.17)),
                        tuple(Category.TRAVEL_EXPENSES, BigDecimal.valueOf(280.34)),
                        tuple(Category.FOOD_AND_DRINKS, BigDecimal.valueOf(433.85)),
                        tuple(Category.OTHER_EXPENSES, BigDecimal.valueOf(184.63)),
                        tuple(Category.PENSION_PAYMENTS, new BigDecimal("524.90")),
                        tuple(Category.INTEREST_AND_REPAYMENTS, BigDecimal.valueOf(665.71)),
                        tuple(Category.EQUITY_WITHDRAWAL, BigDecimal.valueOf(95.22)),
                        tuple(Category.COLLECTION_COSTS, BigDecimal.valueOf(828.67))
                );

        assertThat(recurringTransactionsMonthlyReportRepository.findAllByCreditScoreId(report.getId()))
                .usingElementComparatorIgnoringFields("id")
                .containsOnly(RecurringTransactionsMonthlyReportEntity.builder()
                                .creditScoreId(report.getId())
                                .year(2020)
                                .month(12)
                                .incomeRecurringAmount(new BigDecimal("0"))
                                .incomeRecurringSize(0)
                                .outcomeRecurringAmount(new BigDecimal("69.97"))
                                .outcomeRecurringSize(1)
                                .build(),
                        RecurringTransactionsMonthlyReportEntity.builder()
                                .creditScoreId(report.getId())
                                .year(2020)
                                .month(11)
                                .incomeRecurringAmount(new BigDecimal("149.17"))
                                .incomeRecurringSize(1)
                                .outcomeRecurringAmount(new BigDecimal("0"))
                                .outcomeRecurringSize(0)
                                .build());

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.COMPLETED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .contains(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_SAVED),
                        tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(1, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    @Test
    void shouldUserWithSelectedAccountConfirmCreditScoreReportWithNoTransaction() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setSelectedAccountId(UUID.fromString("eec331b3-ec4b-44e9-8749-209c111b0d82"))
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID);

        creditScoreUserRepository.save(user);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_CONFIRM)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        CreditScoreReport report = creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID).orElseThrow();
        then(report)
                .hasFieldOrPropertyWithValue("accountReference.iban", "NL75INGB1234567890")
                .hasFieldOrPropertyWithValue("currency", "EUR")
                .hasFieldOrPropertyWithValue("initialBalance", new BigDecimal("0.00"))
                .hasFieldOrPropertyWithValue("transactionsSize", 0)
                .hasFieldOrPropertyWithValue("newestTransactionDate", null)
                .hasFieldOrPropertyWithValue("oldestTransactionDate", null)
                .hasFieldOrPropertyWithValue("signatureKeyId", UUID.fromString("0a07c523-86a9-4bf9-9e0f-976beb37bcea"))
                .hasFieldOrPropertyWithValue("accountHolder", null);

        then(report.getSignature()).hasSize(344);

        then(report.getCreditScoreMonthly())
                .isEmpty();

        assertThat(recurringTransactionsMonthlyReportRepository.findAllByCreditScoreId(report.getId())).isEmpty();

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.COMPLETED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .contains(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_SAVED),
                        tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(1, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    @Test
    void shouldSelectedUserRefuseCreditScoreReport() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.ACCOUNT_SELECTED)
                .setYoltUserId(SOME_YOLT_USER_ID);

        creditScoreUserRepository.save(user);

        CreditScoreReport creditScoreReport = prepareCreditScoreUserWithCreditReport(user);
        creditScoreReportRepository.save(creditScoreReport);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_REFUSE)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        then(creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID)).isEmpty();

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.REPORT_SHARING_REFUSED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(
                        tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED),
                        tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_REFUSED));

        verify(1, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    @Test
    void shouldNotUserWithReportShareRefuseToConfirmCreditScoreReport() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.REPORT_SHARING_REFUSED);
        creditScoreUserRepository.save(user);

        CreditScoreReport creditScoreReport = prepareCreditScoreUserWithCreditReport(user);
        creditScoreReportRepository.save(creditScoreReport);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_CONFIRM)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest());

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.REPORT_SHARING_REFUSED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(0, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    @Test
    void shouldNotUserWithReportSharedCompletedToRefuseCreditScoreReport() throws Exception {
        // Given
        CreditScoreUser user = createCreditScoreUser()
                .setStatus(InvitationStatus.COMPLETED);
        creditScoreUserRepository.save(user);

        CreditScoreReport creditScoreReport = prepareCreditScoreUserWithCreditReport(user);
        creditScoreReportRepository.save(creditScoreReport);

        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setUserId(SOME_USER_ID);
        userJourneyMetric.setClientId(SOME_CLIENT_ID);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));
        userJourneyRepository.save(userJourneyMetric);

        // When
        ResultActions perform = mvc.perform(post(CASHFLOW_OVERVIEW_REFUSE)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createUserToken(SOME_USER_HASH))
        );

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest());

        then(creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("accountReference.iban", creditScoreReport.getAccountReference().getIban())
                .hasFieldOrPropertyWithValue("initialBalance", creditScoreReport.getInitialBalance())
                .hasFieldOrPropertyWithValue("currency", creditScoreReport.getCurrency())
                .hasFieldOrPropertyWithValue("transactionsSize", creditScoreReport.getTransactionsSize());

        then(creditScoreUserRepository.findById(SOME_USER_ID))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("status", InvitationStatus.COMPLETED)
                .hasFieldOrPropertyWithValue("yoltUserId", null)
                .hasFieldOrPropertyWithValue("yoltUserSiteId", null)
                .hasFieldOrPropertyWithValue("selectedAccountId", null);

        then(userJourneyRepository.findAll())
                .extracting("userId", "clientId", "status")
                .containsExactly(tuple(SOME_USER_ID, SOME_CLIENT_ID, JourneyStatus.REPORT_GENERATED));

        verify(0, deleteRequestedFor(urlEqualTo("/v1/users/497f6eca-6276-4993-bfeb-53cbbbba6f08")));
    }

    //TODO: Parametric test for old and new data's credit score report schema
    private CreditScoreReport prepareCreditScoreUserWithCreditReport(CreditScoreUser creditScoreUser) {

        List<String> signatureJsonPaths = List.of(
                "$['userId']",
                "$['iban']",
                "$['initialBalance']",
                "$['lastDataFetchTime']",
                "$['currency']",
                "$['newestTransactionDate']",
                "$['oldestTransactionDate']",
                "$['creditLimit']",
                "$['transactionsSize']",
                "$['loans']",
                "$['lines']",
                "$['pdStatus']",
                "$['accountHolder']",
                "$['creditScoreMonthly'][0]['month']",
                "$['creditScoreMonthly'][0]['year']",
                "$['creditScoreMonthly'][0]['highestBalance']",
                "$['creditScoreMonthly'][0]['lowestBalance']",
                "$['creditScoreMonthly'][0]['averageBalance']",
                "$['creditScoreMonthly'][0]['totalIncoming']",
                "$['creditScoreMonthly'][0]['totalOutgoing']",
                "$['creditScoreMonthly'][0]['transactionsSize']"
        );

        CreditScoreReport creditScoreReport = CreditScoreReport.builder()
                .id(UUID.fromString("86139919-4501-4904-9e8d-f7b175898e7f"))
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .initialBalance(new BigDecimal("5000.00"))
                .lastDataFetchTime(SOME_FIXED_TEST_DATE)
                .currency("EUR")
                .transactionsSize(100)
                .creditLimit(new BigDecimal("-1000.00"))
                .newestTransactionDate(LocalDate.of(2020, 12, 31))
                .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                .accountHolder("Account Holder")
                .creditScoreUserId(creditScoreUser.getId())
                .signature("lPU/FtazbMB97o3hZ9gmwpWBvmukJNeGORCmxP+gcRJ0LxAQDXPA1rbucPPWTtWrJFh3T79gHp6+ivGH93ZIO6RpnMSXUhcScsLMCqvmCWSOoIc+p7ktRkcq6h9g69FVxJ2IaOzvTa44EpQPGxyjFXPKC7ew+Ea2XjRYIYeQR8Npd0TRMSsvsPjW4TtP+PyrF1D/tq8doNw4L2wKBoBW5N5hqz0kPw1PPDmMbrU5+3T4aE8CIHxmhzvkSagxYebcwAlWdtDedLkXQtNHPjaM1tuN0JXZMeVTUlOUvW1wSdYmBv2jB7f6amRG2d/vkIUc+oDVFXh/0aMQ0eGJbVcqKw==")
                .signatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .signatureJsonPaths(signatureJsonPaths)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport = CreditScoreMonthlyReport.builder()
                .id(UUID.fromString("9ca84a24-6174-4b83-a525-a9a7545a9b4d"))
                .year(2020)
                .month(12)
                .highestBalance(new BigDecimal("15000.00"))
                .lowestBalance(new BigDecimal("10000.00"))
                .averageBalance(new BigDecimal("12000.00"))
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 2)
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        creditScoreMonthlyReport.setCreditScoreReport(creditScoreReport);
        creditScoreReport.setCreditScoreMonthly(Collections.singleton(creditScoreMonthlyReport));
        return creditScoreReport;
    }

    private static CreditScoreUser createCreditScoreUser() {
        return new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);
    }
}
