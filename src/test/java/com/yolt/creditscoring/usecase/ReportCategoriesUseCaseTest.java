package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.exception.CreditScoreReportNotFoundException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.client.ClientFeatureDisabledException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ID;
import static com.yolt.creditscoring.TestUtils.SOME_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportCategoriesUseCaseTest {

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private ClientStorageService clientService;

    @InjectMocks
    private ReportCategoriesUseCase reportCategoriesUseCase;

    @Mock
    private CreditScoreStorageService creditScoreStorageService;

    @Test
    void shouldThrowExceptionWhenClientFetchUsersCategoriesForDifferentClient() {
        // Given
        UUID someDifferentClientId = UUID.randomUUID();
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder().clientId(someDifferentClientId).build());

        // When
        Throwable thrown = catchThrowable(() -> reportCategoriesUseCase.getUserCategories(SOME_USER_ID, SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUserDoNotHaveAccessToCategories() {
        final TogglesDTO togglesDTO = TogglesDTO.builder()
                .categoryFeatureToggle(false)
                .monthsFeatureToggle(false)
                .overviewFeatureToggle(false)
                .apiTokenFeatureToggle(false)
                .build();

        // Given
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder().clientId(SOME_CLIENT_ID).build());
        given(clientService.getFeatureToggles(SOME_CLIENT_ID))
                .willReturn(togglesDTO);

        // When
        Throwable thrown = catchThrowable(() -> reportCategoriesUseCase.getUserCategories(SOME_USER_ID, SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(ClientFeatureDisabledException.class);
    }

    @Test
    void shouldThrowAppropriateExceptionWhenCreditReportIsNotFoundForAllInOneReport() {
        final TogglesDTO togglesDTO = TogglesDTO.builder()
                .categoryFeatureToggle(true)
                .monthsFeatureToggle(false)
                .overviewFeatureToggle(false)
                .apiTokenFeatureToggle(false)
                .build();

        // Given
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder().clientId(SOME_CLIENT_ID).status(InvitationStatus.INVITED).build());
        given(clientService.getFeatureToggles(SOME_CLIENT_ID))
                .willReturn(togglesDTO);
        given(creditScoreStorageService.getCreditScoreReportBankAccountDetails(SOME_USER_ID))
                .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> reportCategoriesUseCase.getUserCategoriesForAllInOneReport(SOME_USER_ID, SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(CreditScoreReportNotFoundException.class);
    }
}
