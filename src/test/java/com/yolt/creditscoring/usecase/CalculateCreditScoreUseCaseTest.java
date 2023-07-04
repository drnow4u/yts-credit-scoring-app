package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.algorithm.CreditScoreAlgorithm;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsStorageService;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.TotalRecurringTransactionsAggregator;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.estimate.provider.EstimateProvider;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.storage.EstimateStorageService;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.securitymodule.signature.SignatureService;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CalculateCreditScoreUseCaseTest {

    @Mock
    private YoltProvider yoltProvider;

    @Mock
    private EstimateProvider estimateProvider;

    @Mock
    private CreditScoreStorageService creditScoreStorageService;

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private CreditScoreAlgorithm creditScoreAlgorithm;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private ClientStorageService clientService;

    @Mock
    private UserAuditService userAuditService;

    @Mock
    private TotalRecurringTransactionsAggregator totalRecurringTransactionsAggregator;

    @Mock
    private RecurringTransactionsStorageService cycleTransactionsStorage;

    @Mock
    private EstimateStorageService estimateStorageService;

    @InjectMocks
    private CalculateCreditScoreUseCase creditScoreUseCase;

    @BeforeEach
    void setUp() {
        creditScoreUseCase = new CalculateCreditScoreUseCase(
                yoltProvider,
                estimateProvider,
                creditScoreStorageService,
                userStorageService,
                creditScoreAlgorithm,
                userJourneyService,
                signatureService,
                clientService,
                userAuditService,
                totalRecurringTransactionsAggregator,
                cycleTransactionsStorage,
                estimateStorageService
        );
    }

    @Test
    void calculateCreditReportForGivenAccountPDFeatureToggleOn() {
        // Given
        ArgumentCaptor<ReportSaveDTO> creditScoreReportArgumentCaptor = ArgumentCaptor.forClass(ReportSaveDTO.class);

        given(yoltProvider.hasUserDataLoadedCompletely(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACTIVITY_ID))
                .willReturn(true);
        CreditScoreAccountDTO creditScoreAccountDTO = creditScoreAccountDTO();
        given(yoltProvider.getAccountForCreditScoreCalculations(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACCOUNT_ID))
                .willReturn(creditScoreAccountDTO);

        ReportSaveDTO report = ReportSaveDTO.builder()
                .initialBalance(new BigDecimal("5000.00"))
                .newestTransactionDate(LocalDate.of(2021, 1, 25))
                .oldestTransactionDate(LocalDate.of(2020, 11, 2))
                .iban("NL79ABNA12345678901")
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .creditScoreMonthly(Set.of(MonthlyReportSaveDTO.builder()
                        .year(2021)
                        .month(1)
                        .highestBalance(new BigDecimal("5750.00"))
                        .lowestBalance(new BigDecimal("3750.00"))
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("2000.00"))
                                .category(Category.OTHER_INCOME)
                                .build())
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("-750.00"))
                                .category(Category.OTHER_EXPENSES)
                                .build())
                        .build()))
                .build();

        given(creditScoreAlgorithm.calculateCreditReport(any())).willReturn(report);

        given(signatureService.sign(report))
                .willReturn(ReportSignature.builder()
                        .signature(SOME_REPORT_SIGNATURE)
                        .keyId(SOME_REPORT_SIGNATURE_KEY_ID)
                        .jsonPaths(List.of("$['userId']", "$['iban']", "$['initialBalance']"))
                        .build());

        given(clientService.checkIfClientHasPDFeatureEnabled(SOME_CLIENT_ID)).willReturn(true);

        given(estimateProvider.calculatePDForReport(creditScoreAccountDTO))
                .willReturn(ProbabilityOfDefaultStorage.builder()
                        .score(29)
                        .grade(RiskClassification.G)
                        .status(PdStatus.COMPLETED)
                        .build());

        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .selectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                        .yoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                        .build());

        // When
        var status = creditScoreUseCase.calculateCreditReportForGivenAccount(SOME_USER_ID);

        // Then
        assertThat(status).isTrue();
        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);
        then(creditScoreStorageService).should().saveCreditScoreReportForGivenUser(creditScoreReportArgumentCaptor.capture(), any(ReportSignature.class), eq(SOME_USER_ID));
        ReportSaveDTO result = creditScoreReportArgumentCaptor.getValue();
        assertThat(result.getInitialBalance()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(result.getNewestTransactionDate()).isEqualTo(LocalDate.of(2021, 1, 25));
        assertThat(result.getOldestTransactionDate()).isEqualTo(LocalDate.of(2020, 11, 2));
        assertThat(result.getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(result.getCreditLimit()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.getTransactionsSize()).isEqualTo(10);

        assertThat(result.getCreditScoreMonthly()).extracting("year", "month", "highestBalance",
                        "lowestBalance", "categoriesAmounts")
                .containsOnly(
                        tuple(2021,
                                1,
                                new BigDecimal("5750.00"),
                                new BigDecimal("3750.00"),
                                List.of(
                                        MonthlyCategoryReportSaveDTO.builder()
                                                .amount(new BigDecimal("2000.00"))
                                                .category(Category.OTHER_INCOME)
                                                .build(),
                                        MonthlyCategoryReportSaveDTO.builder()
                                                .amount(new BigDecimal("-750.00"))
                                                .category(Category.OTHER_EXPENSES)
                                                .build()
                                )
                        )
                );
        then(userJourneyService).should().registerReportGenerated(eq(SOME_CLIENT_ID), eq(SOME_USER_ID));
        then(estimateProvider).should().calculatePDForReport(creditScoreAccountDTO);
        then(userStorageService).should().removeYoltUser(SOME_USER_ID);
        then(estimateStorageService).should().save(eq(SOME_USER_ID), eq(ProbabilityOfDefaultStorage.builder()
                .score(29)
                .grade(RiskClassification.G)
                .status(PdStatus.COMPLETED)
                .build()));
    }
    @Test
    void calculateCreditReportForGivenAccountPDFeatureToggleOff() {
        // Given
        ArgumentCaptor<ReportSaveDTO> creditScoreReportArgumentCaptor = ArgumentCaptor.forClass(ReportSaveDTO.class);

        given(yoltProvider.hasUserDataLoadedCompletely(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACTIVITY_ID))
                .willReturn(true);
        CreditScoreAccountDTO creditScoreAccountDTO = creditScoreAccountDTO();
        given(yoltProvider.getAccountForCreditScoreCalculations(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACCOUNT_ID))
                .willReturn(creditScoreAccountDTO);

        ReportSaveDTO report = ReportSaveDTO.builder()
                .initialBalance(new BigDecimal("5000.00"))
                .newestTransactionDate(LocalDate.of(2021, 1, 25))
                .oldestTransactionDate(LocalDate.of(2020, 11, 2))
                .iban("NL79ABNA12345678901")
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .creditScoreMonthly(Set.of(MonthlyReportSaveDTO.builder()
                        .year(2021)
                        .month(1)
                        .highestBalance(new BigDecimal("5750.00"))
                        .lowestBalance(new BigDecimal("3750.00"))
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("2000.00"))
                                .category(Category.OTHER_INCOME)
                                .build())
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("-750.00"))
                                .category(Category.OTHER_EXPENSES)
                                .build())
                        .build()))
                .build();

        given(creditScoreAlgorithm.calculateCreditReport(any())).willReturn(report);

        given(signatureService.sign(report))
                .willReturn(ReportSignature.builder()
                        .signature(SOME_REPORT_SIGNATURE)
                        .keyId(SOME_REPORT_SIGNATURE_KEY_ID)
                        .jsonPaths(List.of("$['userId']", "$['iban']", "$['initialBalance']"))
                        .build());

        given(clientService.checkIfClientHasPDFeatureEnabled(SOME_CLIENT_ID)).willReturn(false);

        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .selectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                        .yoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                        .build());

        // When
        var status = creditScoreUseCase.calculateCreditReportForGivenAccount(SOME_USER_ID);

        // Then
        assertThat(status).isTrue();
        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);
        then(creditScoreStorageService).should().saveCreditScoreReportForGivenUser(creditScoreReportArgumentCaptor.capture(), any(ReportSignature.class), eq(SOME_USER_ID));
        ReportSaveDTO result = creditScoreReportArgumentCaptor.getValue();
        assertThat(result.getInitialBalance()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(result.getNewestTransactionDate()).isEqualTo(LocalDate.of(2021, 1, 25));
        assertThat(result.getOldestTransactionDate()).isEqualTo(LocalDate.of(2020, 11, 2));
        assertThat(result.getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(result.getCreditLimit()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.getTransactionsSize()).isEqualTo(10);

        assertThat(result.getCreditScoreMonthly()).extracting("year", "month", "highestBalance",
                        "lowestBalance", "categoriesAmounts")
                .containsOnly(
                        tuple(2021,
                                1,
                                new BigDecimal("5750.00"),
                                new BigDecimal("3750.00"),
                                List.of(
                                        MonthlyCategoryReportSaveDTO.builder()
                                                .amount(new BigDecimal("2000.00"))
                                                .category(Category.OTHER_INCOME)
                                                .build(),
                                        MonthlyCategoryReportSaveDTO.builder()
                                                .amount(new BigDecimal("-750.00"))
                                                .category(Category.OTHER_EXPENSES)
                                                .build()
                                )
                        )
                );
        then(userJourneyService).should().registerReportGenerated(eq(SOME_CLIENT_ID), eq(SOME_USER_ID));
        then(estimateProvider).should(never()).calculatePDForReport(any());
        then(userStorageService).should().removeYoltUser(SOME_USER_ID);
        then(estimateProvider).should(never()).calculatePDForReport(any());
        then(estimateStorageService).should(never()).save(any(), any());
    }

    @Test
    void shouldSetStatusErrorForUserWhenReportWillNotGenerateSuccessfully() {
        // Given
        given(yoltProvider.hasUserDataLoadedCompletely(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACTIVITY_ID))
                .willReturn(true);
        given(yoltProvider.getAccountForCreditScoreCalculations(SOME_YOLT_USER_ID, SOME_YOLT_USER_ACCOUNT_ID))
                .willThrow(new RuntimeException("Some Exception"));

        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .selectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                        .yoltActivityId(SOME_YOLT_USER_ACTIVITY_ID)
                        .build());

        // When
        var status = creditScoreUseCase.calculateCreditReportForGivenAccount(SOME_USER_ID);

        // Then
        assertThat(status).isTrue();
        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);
        then(userStorageService).should().calculationError(SOME_USER_ID);
        then(userStorageService).should(never()).complete(SOME_USER_ID);
        then(creditScoreStorageService).should(never()).saveCreditScoreReportForGivenUser(
                any(ReportSaveDTO.class),
                any(ReportSignature.class),
                eq(SOME_USER_ID));
        then(estimateStorageService).should(never()).save(any(), any());

        then(userJourneyService).should(never()).registerReportGenerated(any(), any());
        then(userStorageService).should().removeYoltUser(SOME_USER_ID);
    }

    private CreditScoreAccountDTO creditScoreAccountDTO() {
        List<CreditScoreTransactionDTO> transactions = Arrays.asList(
                createTransaction("-50.00", LocalDate.of(2021, 1, 25)),
                createTransaction("-200.00", LocalDate.of(2021, 1, 20)),
                createTransaction("-500.00", LocalDate.of(2021, 1, 13)),
                createTransaction("2000.00", LocalDate.of(2021, 1, 10)),

                createTransaction("100.00", LocalDate.of(2020, 12, 22)),
                createTransaction("-200.00", LocalDate.of(2020, 12, 13)),
                createTransaction("2000.00", LocalDate.of(2020, 12, 12)),
                createTransaction("-800.00", LocalDate.of(2020, 12, 10)),

                createTransaction("2000.00", LocalDate.of(2020, 11, 10)),
                createTransaction("-1700.00", LocalDate.of(2020, 11, 2))
        );

        return CreditScoreAccountDTO.builder()
                .balance(new BigDecimal("5000.00"))
                .transactions(transactions)
                .creditLimit(new BigDecimal("1000.00"))
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .build();
    }

    private CreditScoreTransactionDTO createTransaction(String amount, LocalDate localDate) {
        return CreditScoreTransactionDTO.builder()
                .amount(new BigDecimal(amount))
                .date(localDate)
                .build();
    }
}
