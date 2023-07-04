package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.exception.CreditScoreReportNotFoundException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.client.ClientFeatureDisabledException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.category.CategoryService;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowBegin;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowEnd;

@UseCase
@RequiredArgsConstructor
public class ReportCategoriesUseCase {

    private final CategoryService categoryService;
    private final UserStorageService userStorageService;
    private final ClientStorageService clientService;
    private final CreditScoreStorageService creditScoreStorageService;

    public List<SMECategoryDTO> getUserCategories(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = validateUser(userId, clientId);

        BankAccountDetailsDTO bankAccountDetailsDTO = creditScoreStorageService.getCreditScoreReportBankAccountDetails(userId)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "Credit score report was not found for user id: " + userId));

        return getCategoryDTOS(userId, bankAccountDetailsDTO);
    }

    public List<SMECategoryDTO> getUserCategoriesForAllInOneReport(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = validateUser(userId, clientId);

        BankAccountDetailsDTO bankAccountDetailsDTO = creditScoreStorageService.getCreditScoreReportBankAccountDetails(userId)
                .orElseThrow(() -> new CreditScoreReportNotFoundException(user.getStatus(), "User was not found with userID: " + userId + "and clientID: " + clientId));

        return getCategoryDTOS(userId, bankAccountDetailsDTO);
    }

    private CreditScoreUserDTO validateUser(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        final TogglesDTO toggles = clientService.getFeatureToggles(clientId);

        if (!toggles.isCategoryFeatureToggle()) {
            throw new ClientFeatureDisabledException("Client does not have access to category feature");
        }
        return user;
    }

    private List<SMECategoryDTO> getCategoryDTOS(UUID userId, BankAccountDetailsDTO bankAccountDetailsDTO) {
        final LocalDate reportFetchTime = bankAccountDetailsDTO.getLastDataFetchTime().toLocalDate();
        return new ArrayList<>(categoryService.getCategoriesForUser(userId, windowBegin(reportFetchTime), windowEnd(reportFetchTime)).values());
    }
}
