package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface RecurringTransactionsMonthlyReportRepository extends CrudRepository<RecurringTransactionsMonthlyReportEntity, UUID> {

    Set<RecurringTransactionsMonthlyReportEntity> findAllByCreditScoreId(UUID creditScoreId);

    void deleteAllByCreditScoreId(UUID creditScoreId);
}
