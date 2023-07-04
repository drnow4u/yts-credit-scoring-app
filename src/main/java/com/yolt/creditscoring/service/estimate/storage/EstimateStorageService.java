package com.yolt.creditscoring.service.estimate.storage;

import com.yolt.creditscoring.controller.admin.estimate.EstimateReportNotFound;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class EstimateStorageService {

    private final EstimateRepository creditScoreReportRepository;

    public @Valid ProbabilityOfDefaultStorage getRiskReport(@NonNull UUID userId) {
        return creditScoreReportRepository.findByUserId(userId)
                .map(report -> ProbabilityOfDefaultStorage.builder()
                        .score(report.getScore())
                        .grade(report.getGrade())
                        .status(report.getStatus())
                        .build())
                .orElseThrow(() ->
                        new EstimateReportNotFound("Credit report was not calculated for user " + userId));
    }

    public void save(UUID userId, ProbabilityOfDefaultStorage pdresult) {
        creditScoreReportRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(userId)
                .setGrade(pdresult.getGrade())
                .setScore(pdresult.getScore())
                .setStatus(pdresult.getStatus()));

    }
}
