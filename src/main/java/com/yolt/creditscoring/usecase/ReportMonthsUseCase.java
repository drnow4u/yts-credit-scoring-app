package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.controller.admin.estimate.FeatureToggleDisableException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.UUID;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class ReportMonthsUseCase {

    private final UserStorageService userStorageService;
    private final ClientStorageService clientService;
    private final CreditScoreStorageService creditScoreStorageService;

    public Set<MonthlyAdminReportDTO> getUserCreditScoreMonths(@NonNull UUID clientId, @NonNull UUID userId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        final TogglesDTO toggles = clientService.getFeatureToggles(clientId);

        if (!toggles.isMonthsFeatureToggle()) {
            throw new FeatureToggleDisableException("Client does not have access to months feature");
        }

        return creditScoreStorageService.getCreditScoreMonthsDTO(user);
    }

}
