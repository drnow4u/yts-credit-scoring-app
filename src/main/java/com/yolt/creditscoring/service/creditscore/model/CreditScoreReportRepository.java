package com.yolt.creditscoring.service.creditscore.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditScoreReportRepository extends CrudRepository<CreditScoreReport, UUID> {

    Optional<CreditScoreReport> findByCreditScoreUserId(UUID creditScoreUserId);

    @Query("select c.id from CreditScoreReport c where c.creditScoreUserId = ?1")
    Optional<UUID> getCreditScoreReportIDByUserId(UUID creditScoreUserId);

    void deleteByCreditScoreUserId(UUID creditScoreUserId);

    List<CreditScoreReport> findBySignatureIsNull();
}
