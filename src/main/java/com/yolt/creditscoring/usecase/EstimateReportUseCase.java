package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.controller.admin.estimate.FeatureToggleDisableException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.storage.EstimateStorageService;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.usecase.dto.RiskClassificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class EstimateReportUseCase {

    private final UserStorageService userStorageService;
    private final ClientStorageService clientService;
    private final EstimateStorageService estimateStorageService;

    private final Pair<Double, Double> RATE_A = Pair.of(0.0, 0.5);
    private final Pair<Double, Double> RATE_B = Pair.of(0.5, 1.5);
    private final Pair<Double, Double> RATE_C = Pair.of(1.5, 2.5);
    private final Pair<Double, Double> RATE_D = Pair.of(2.5, 3.5);
    private final Pair<Double, Double> RATE_E = Pair.of(3.5, 4.5);
    private final Pair<Double, Double> RATE_F = Pair.of(4.5, 5.5);
    private final Pair<Double, Double> RATE_G = Pair.of(5.5, 6.5);
    private final Pair<Double, Double> RATE_H = Pair.of(6.5, 7.5);
    private final Pair<Double, Double> RATE_I = Pair.of(7.5, 8.5);
    private final Pair<Double, Double> RATE_J = Pair.of(8.5, null);

    private final Map<RiskClassification, Pair<Double, Double>> table = Map.of(
            RiskClassification.A, RATE_A,
            RiskClassification.B, RATE_B,
            RiskClassification.C, RATE_C,
            RiskClassification.D, RATE_D,
            RiskClassification.E, RATE_E,
            RiskClassification.F, RATE_F,
            RiskClassification.G, RATE_G,
            RiskClassification.H, RATE_H,
            RiskClassification.I, RATE_I,
            RiskClassification.J, RATE_J
    );

    public @Valid RiskClassificationDTO getUserRiskScore(UUID userId, UUID clientId) {
        if (!clientService.checkIfClientHasPDFeatureEnabled(clientId)) {
            throw new FeatureToggleDisableException("Estimate PD feature toggle is disable for client " + clientId);
        }

        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        ProbabilityOfDefaultStorage estimateReport = estimateStorageService.getRiskReport(userId);

        if (estimateReport.getStatus() == PdStatus.ERROR || estimateReport.getStatus() == PdStatus.ERROR_NOT_ENOUGH_TRANSACTIONS) {
            return RiskClassificationDTO.createError(estimateReport.getStatus());
        }

        final Pair<Double, Double> rate = table.get(estimateReport.getGrade());

        return new RiskClassificationDTO(
                rate.getLeft(),
                rate.getRight(),
                estimateReport.getGrade(),
                estimateReport.getStatus()
        );
    }

}
