package com.yolt.creditscoring.service.estimate.provider;

import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimatePDRequestDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimateProbabilityOfDefaultDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import com.yolt.creditscoring.service.estimate.provider.exception.NotEnoughTransactionDataException;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateProvider {

    private final EstimateHttpClient httpClient;

    public ProbabilityOfDefaultStorage calculatePDForReport(CreditScoreAccountDTO account) {
        try {
            EstimatePDRequestDTO request = EstimatePDRequestCreator.createRequest(account);

            EstimateProbabilityOfDefaultDTO scoreForGivenAccount = httpClient.getPDScoreForGivenAccount(request);

            return ProbabilityOfDefaultStorage.builder()
                    .score(scoreForGivenAccount.getScore())
                    .grade(scoreForGivenAccount.getGrade())
                    .status(PdStatus.COMPLETED)
                    .build();
        } catch (NotEnoughTransactionDataException e) {
            return ProbabilityOfDefaultStorage.builder()
                    .status(PdStatus.ERROR_NOT_ENOUGH_TRANSACTIONS)
                    .build();
        } catch (Exception e) {
            log.error("There was an error when fetching PD score from Estimate API", e);
            return ProbabilityOfDefaultStorage.builder()
                    .status(PdStatus.ERROR)
                    .build();
        }
    }
}
