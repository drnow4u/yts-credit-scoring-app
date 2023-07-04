package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionsStorageService {

    private final RecurringTransactionsMonthlyReportRepository cycleTransactionsMonthlyReportRepository;

    public void saveRecurringTransactionsForReport(UUID creditReportId,
                                               Set<MonthlyRecurringTransactionsDTO> monthlyCycleTransactionsReportSaveDTOs) {

        List<RecurringTransactionsMonthlyReportEntity> recurringTransactionsMonthlyReports = monthlyCycleTransactionsReportSaveDTOs.stream()
                .map(monthlyRecurringTransactions ->
                        RecurringTransactionsMonthlyReportEntity.builder()
                                .id(UUID.randomUUID())
                                .creditScoreId(creditReportId)
                                .year(monthlyRecurringTransactions.getYear())
                                .month(monthlyRecurringTransactions.getMonth())
                                .incomeRecurringAmount(monthlyRecurringTransactions.getIncomeRecurringAmount())
                                .incomeRecurringSize(monthlyRecurringTransactions.getIncomeRecurringSize())
                                .outcomeRecurringAmount(monthlyRecurringTransactions.getOutcomeRecurringAmount())
                                .outcomeRecurringSize(monthlyRecurringTransactions.getOutcomeRecurringSize())
                                .build())
                .toList();

        cycleTransactionsMonthlyReportRepository.saveAll(recurringTransactionsMonthlyReports);
    }

    public List<MonthlyRecurringTransactionsDTO> getMonthlyRecurringTransactionsReportSaveDTOs(UUID creditReportId) {
        return cycleTransactionsMonthlyReportRepository.findAllByCreditScoreId(creditReportId).stream()
                .map(monthlyRecurringTransactions ->
                        MonthlyRecurringTransactionsDTO.builder()
                                .year(monthlyRecurringTransactions.getYear())
                                .month(monthlyRecurringTransactions.getMonth())
                                .incomeRecurringAmount(monthlyRecurringTransactions.getIncomeRecurringAmount())
                                .incomeRecurringSize(monthlyRecurringTransactions.getIncomeRecurringSize())
                                .outcomeRecurringAmount(monthlyRecurringTransactions.getOutcomeRecurringAmount())
                                .outcomeRecurringSize(monthlyRecurringTransactions.getOutcomeRecurringSize())
                                .build())
                .toList();
    }
}
