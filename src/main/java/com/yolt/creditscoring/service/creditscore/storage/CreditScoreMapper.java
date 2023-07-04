package com.yolt.creditscoring.service.creditscore.storage;

import com.yolt.creditscoring.service.creditscore.model.*;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.OverviewInfoDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CreditScoreMapper {

    /**
     * Mapping from ReportSaveDTO to CreditScoreReport entity
     *
     * @param calculatedSignature signature that will be stored in CreditScoreReport
     * @return mapped CreditScoreReport
     */
    public static CreditScoreReport mapReportSaveToEntity(ReportSaveDTO reportSaveDTO,
                                                          ReportSignature calculatedSignature) {
        return CreditScoreReport.builder()
                .id(UUID.randomUUID())
                .accountReference(AccountReference.builder()
                        .iban(reportSaveDTO.getIban())
                        .bban(reportSaveDTO.getBban())
                        .sortCodeAccountNumber(reportSaveDTO.getSortCodeAccountNumber())
                        .maskedPan(reportSaveDTO.getMaskedPan())
                        .build())
                .initialBalance(reportSaveDTO.getInitialBalance())
                .lastDataFetchTime(reportSaveDTO.getLastDataFetchTime())
                .currency(reportSaveDTO.getCurrency())
                .newestTransactionDate(reportSaveDTO.getNewestTransactionDate())
                .oldestTransactionDate(reportSaveDTO.getOldestTransactionDate())
                .creditLimit(reportSaveDTO.getCreditLimit())
                .transactionsSize(reportSaveDTO.getTransactionsSize())
                .accountHolder(reportSaveDTO.getAccountHolder())
                .signature(calculatedSignature.getSignature().toString())
                .signatureKeyId(calculatedSignature.getKeyId())
                .signatureJsonPaths(calculatedSignature.getJsonPaths())
                .creditScoreMonthly(
                        reportSaveDTO.getCreditScoreMonthly().stream()
                                .map(CreditScoreMapper::mapToMonthlyReportEntity)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    private static CreditScoreMonthlyReport mapToMonthlyReportEntity(MonthlyReportSaveDTO dto) {
        return CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(dto.getYear())
                .month(dto.getMonth())
                .highestBalance(dto.getHighestBalance())
                .lowestBalance(dto.getLowestBalance())
                .averageBalance(dto.getAverageBalance())
                .incomingTransactionsSize(dto.getIncomingTransactionsSize())
                .outgoingTransactionsSize(dto.getOutgoingTransactionsSize())
                .categorizedAmounts(toCategorizedAmounts(dto.getCategoriesAmounts()))
                .build();
    }

    private static Map<Category, CategorizedAmountEntity> toCategorizedAmounts(List<MonthlyCategoryReportSaveDTO> categorizedAmounts) {
        return categorizedAmounts.stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getCategory(), new CategorizedAmountEntity(UUID.randomUUID(), entry.getAmount(), entry.getTotalTransactions(), entry.getCategory(), null)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /**
     * Map CreditScoreReport entity to BankAccountDetailsDTO
     *
     * @param userId user ID added to BankAccountDetailsDTO
     * @return mapped BankAccountDetailsDTO
     */
    public static BankAccountDetailsDTO mapCreditScoreReportToAdminReportDTO(CreditScoreReport creditScoreReport, UUID userId) {
        return BankAccountDetailsDTO.builder()
                .userId(userId)
                .initialBalance(creditScoreReport.getInitialBalance())
                .lastDataFetchTime(creditScoreReport.getLastDataFetchTime())
                .newestTransactionDate(creditScoreReport.getNewestTransactionDate())
                .oldestTransactionDate(creditScoreReport.getOldestTransactionDate())
                .currency(creditScoreReport.getCurrency())
                .iban(creditScoreReport.getAccountReference().getIban())
                .bban(creditScoreReport.getAccountReference().getBban())
                .sortCodeAccountNumber(creditScoreReport.getAccountReference().getSortCodeAccountNumber())
                .maskedPan(creditScoreReport.getAccountReference().getMaskedPan())
                .creditLimit(creditScoreReport.getCreditLimit())
                .transactionsSize(creditScoreReport.getTransactionsSize())
                .accountHolder(creditScoreReport.getAccountHolder())
                .build();

    }

    protected static Set<MonthlyAdminReportDTO> mapCreditScoreMonthlyReportToMonthlyAdminReportDTO(Set<CreditScoreMonthlyReport> creditScoreMonthly) {
        return creditScoreMonthly.stream()
                .map(csm -> MonthlyAdminReportDTO.builder()
                        .year(csm.getYear())
                        .month(csm.getMonth())
                        .highestBalance(csm.getHighestBalance())
                        .lowestBalance(csm.getLowestBalance())
                        .averageBalance(csm.getAverageBalance())
                        .incomingTransactionsSize(csm.getIncomingTransactionsSize())
                        .outgoingTransactionsSize(csm.getOutgoingTransactionsSize())
                        .totalIncoming(totalIncoming(csm.getCategorizedAmounts()))
                        .totalOutgoing(totalOutgoing(csm.getCategorizedAmounts()))
                        .build())
                .sorted(Comparator.comparingInt(MonthlyAdminReportDTO::getYear)
                        .thenComparing(MonthlyAdminReportDTO::getMonth))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static BigDecimal totalIncoming(Map<Category, CategorizedAmountEntity> categorizedAmounts) {
        return categorizedAmounts.values().stream()
                .filter(categoryReport -> Category.isIncomeCategory(categoryReport.getCategory()))
                .map(CategorizedAmountEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalOutgoing(Map<Category, CategorizedAmountEntity> categorizedAmounts) {
        return categorizedAmounts.values().stream()
                .filter(categoryReport -> Category.isExpenseCategory(categoryReport.getCategory()))
                .map(CategorizedAmountEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static OverviewInfoDTO mapCreditScoreReportToOverviewInfoDTO(CreditScoreReport creditScoreReport) {
        return OverviewInfoDTO.builder()
                .lastDataFetchTime(creditScoreReport.getLastDataFetchTime())
                .build();
    }
}
