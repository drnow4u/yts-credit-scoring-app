package com.yolt.creditscoring.service.estimate.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EstimateRepository extends JpaRepository<EstimateEntity, UUID> {

    Optional<EstimateEntity> findByUserId(UUID userId);
}
