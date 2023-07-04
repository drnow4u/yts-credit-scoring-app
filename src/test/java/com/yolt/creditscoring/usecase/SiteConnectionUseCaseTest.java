package com.yolt.creditscoring.usecase;


import com.yolt.creditscoring.controller.user.account.Account;
import com.yolt.creditscoring.controller.user.site.SiteLoginStepDTO;
import com.yolt.creditscoring.controller.user.site.SiteViewDTO;
import com.yolt.creditscoring.exception.UserSiteAlreadyExistException;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.ConsentStep;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.LoginResponse;
import com.yolt.creditscoring.service.yoltapi.exception.SiteAuthenticationException;
import com.yolt.creditscoring.service.yoltapi.exception.SiteCreationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SiteConnectionUseCaseTest {

    @Mock
    private YoltProvider yoltProvider;

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private ClientStorageService clientService;

    @Mock
    private UserAuditService userAuditService;

    @InjectMocks
    private SiteConnectionUseCase siteConnectionUseCase;

    @Test
    void shouldFetchSitesForClientBasedOnTags() {
        // Given
        UUID expectedBankId = UUID.fromString("b199632d-4e08-4a8e-b781-15043871c9e9");
        String expectedBankName = "Some Bank";
        given(clientService.getSiteTagsForGivenClient(SOME_CLIENT_ID)).willReturn(SOME_CLIENT_SITE_TAGS);
        given(yoltProvider.getSites(SOME_CLIENT_SITE_TAGS)).willReturn(
                Collections.singletonList(
                        SiteViewDTO.builder().id(expectedBankId).name(expectedBankName).build()
                ));

        // When
        List<SiteViewDTO> results = siteConnectionUseCase.getSitesForClient(SOME_CLIENT_ID);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(expectedBankId);
        assertThat(results.get(0).getName()).isEqualTo(expectedBankName);
    }

    @Test
    void shouldUserRequestConsent() {
        // Given
        given(yoltProvider.requestUserConsent(SOME_YOLT_USER_ID, SOME_YOLT_SITE_ID, "192.168.0.1"))
                .willReturn(ConsentStep.builder()
                        .redirectUrl("http://yolt.com/")
                        .userSiteId(SOME_YOLT_USER_SITE_ID)
                        .build());

        // When
        SiteLoginStepDTO siteLoginStepDTO = siteConnectionUseCase.requestUserConsent(
                SOME_USER_ID, SOME_YOLT_USER_ID, null, SOME_YOLT_SITE_ID, "192.168.0.1");

        // Then
        CreditScoreUser creditScoreUserExpected = new CreditScoreUser();
        creditScoreUserExpected.setId(SOME_USER_ID);
        creditScoreUserExpected.setName(SOME_USER_NAME);
        creditScoreUserExpected.setEmail(SOME_USER_EMAIL);
        creditScoreUserExpected.setDateTimeInvited(SOME_TEST_DATE);
        creditScoreUserExpected.setDateTimeStatusChange(SOME_TEST_DATE);
        creditScoreUserExpected.setStatus(InvitationStatus.INVITED);
        creditScoreUserExpected.setInvitationHash(SOME_USER_HASH);
        creditScoreUserExpected.setClientId(SOME_CLIENT_ID);
        creditScoreUserExpected.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUserExpected.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        assertThat(siteLoginStepDTO).isEqualTo(SiteLoginStepDTO.builder()
                .redirectUrl("http://yolt.com/")
                .build());

        then(userStorageService)
                .should()
                .updateUserSite(SOME_USER_ID, SOME_YOLT_USER_SITE_ID);
    }

    @Test
    void shouldCreateUserSite() {
        // Given
        given(yoltProvider.createUserSite(SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, SOME_USER_IP))
                .willReturn(LoginResponse.builder()
                        .activityId(SOME_YOLT_USER_ACTIVITY_ID)
                        .build());

        // When
        LoginResponse loginResponse = siteConnectionUseCase.createUserSite(
                SOME_USER_ID, SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, SOME_USER_IP, SOME_CLIENT_ID);

        // Then
        CreditScoreUser creditScoreUserExpected = new CreditScoreUser();
        creditScoreUserExpected.setId(SOME_USER_ID);
        creditScoreUserExpected.setName(SOME_USER_NAME);
        creditScoreUserExpected.setEmail(SOME_USER_EMAIL);
        creditScoreUserExpected.setDateTimeInvited(SOME_TEST_DATE);
        creditScoreUserExpected.setDateTimeStatusChange(SOME_TEST_DATE);
        creditScoreUserExpected.setStatus(InvitationStatus.INVITED);
        creditScoreUserExpected.setInvitationHash(SOME_USER_HASH);
        creditScoreUserExpected.setClientId(SOME_CLIENT_ID);
        creditScoreUserExpected.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUserExpected.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);
        creditScoreUserExpected.setYoltActivityId(SOME_YOLT_USER_ACTIVITY_ID);

        assertThat(loginResponse).isEqualTo(LoginResponse.builder()
                .activityId(SOME_YOLT_USER_ACTIVITY_ID)
                .build());

        then(userStorageService)
                .should()
                .updateActivityId(SOME_USER_ID, SOME_YOLT_USER_ACTIVITY_ID);
        then(userAuditService)
                .should()
                .logBankSelected(SOME_CLIENT_ID, SOME_USER_ID, SOME_USER_IP);
    }

    @Test
    void shouldSetUserStatusToBankConsentRefusedAndThrowSiteCreationExceptionForAuthenticationFailure() {
        // Given
        given(yoltProvider.createUserSite(SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, "192.168.0.1"))
                .willReturn(LoginResponse.builder()
                        .dataFetchFailureReason("AUTHENTICATION_FAILED")
                        .build());

        // When
        Throwable thrown = catchThrowable(() -> siteConnectionUseCase.createUserSite(
                SOME_USER_ID, SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, "192.168.0.1", SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(SiteAuthenticationException.class)
                .hasMessageContaining("User did not accepted consent on bank page");
        then(userStorageService).should().refusedBankConsent(SOME_USER_ID);
    }

    @Test
    void shouldSetUserStatusToBankErrorAndThrowSiteCreationExceptionAnyOtherReasonThenForAuthenticationFailure() {
        // Given
        given(yoltProvider.createUserSite(SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, "192.168.0.1"))
                .willReturn(LoginResponse.builder()
                        .dataFetchFailureReason("TECHNICAL_ERROR")
                        .build());

        // When
        Throwable thrown = catchThrowable(() -> siteConnectionUseCase.createUserSite(
                SOME_USER_ID, SOME_YOLT_USER_ID, SOME_YOLT_REDIRECT_BACK_URL, "192.168.0.1", SOME_CLIENT_ID));

        // Then
        assertThat(thrown).isInstanceOf(SiteCreationException.class)
                .hasMessageContaining("UserSite was not created due to: TECHNICAL_ERROR");
        then(userStorageService).should().bankError(SOME_USER_ID);
    }

    @Test
    void shouldNotCreateUserSiteIfAlreadyExistsForGivenUser() {
        // When
        Throwable thrown = catchThrowable(() -> siteConnectionUseCase.requestUserConsent(
                SOME_USER_ID, SOME_YOLT_USER_ID, SOME_YOLT_USER_SITE_ID, SOME_YOLT_SITE_ID, "192.168.0.1"));

        // Then
        then(yoltProvider).should(never()).requestUserConsent(SOME_YOLT_USER_ID, SOME_YOLT_SITE_ID, "192.168.0.1");
        assertThat(thrown).isInstanceOf(UserSiteAlreadyExistException.class);
    }

    @Test
    void shouldFilterOutAccountsWithEmptyAccountReference() {
        // Given
        given(yoltProvider.getAccounts(SOME_YOLT_USER_ID)).willReturn(
                Collections.singletonList(CreditScoreAccountDTO.builder()
                        .accountReference(AccountReference.builder().build())
                        .build())
        );

        // When
        List<Account> results = siteConnectionUseCase.getAccounts(SOME_YOLT_USER_ID);

        // Then
        assertThat(results).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("accountReferencesWithExpectedAccountNumberResult")
    void shouldCorrectlyMapAllAccountReferencesWithCorrectOrderToAccountNumber(AccountReference accountReference, String accountNumber) {
        // Given
        given(yoltProvider.getAccounts(SOME_YOLT_USER_ID)).willReturn(
                Collections.singletonList(CreditScoreAccountDTO.builder()
                        .accountReference(accountReference)
                        .build())
        );

        // When
        List<Account> results = siteConnectionUseCase.getAccounts(SOME_YOLT_USER_ID);

        // Then
        assertThat(results.get(0).getAccountNumber()).isEqualTo(accountNumber);
    }

    private static Stream<Arguments> accountReferencesWithExpectedAccountNumberResult() {
        return Stream.of(
                Arguments.of(AccountReference.builder().iban("SOME_IBAN").build(), "SOME_IBAN"),
                Arguments.of(AccountReference.builder().bban("SOME_BBAN").build(), "SOME_BBAN"),
                Arguments.of(AccountReference.builder().sortCodeAccountNumber("SOME_SORT_CODE").build(), "SOME_SORT_CODE"),
                Arguments.of(AccountReference.builder().maskedPan("SOME_MASKED_PAN").build(), "SOME_MASKED_PAN"),
                Arguments.of(AccountReference.builder().iban("SOME_IBAN").sortCodeAccountNumber("SOME_SORT_CODE").build(), "SOME_IBAN"),
                Arguments.of(AccountReference.builder().sortCodeAccountNumber("SOME_SORT_CODE").bban("SOME_SORT_CODE").build(), "SOME_SORT_CODE"),
                Arguments.of(AccountReference.builder().bban("SOME_BBAN").maskedPan("SOME_SORT_CODE").build(), "SOME_BBAN"),
                Arguments.of(AccountReference.builder().sortCodeAccountNumber("SOME_SORT_CODE").maskedPan("SOME_MASKED_PAN").build(), "SOME_SORT_CODE")
        );
    }

    @Test
    void shouldUserSelectAccount() {
        // Given
        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .build());

        given(yoltProvider.getAccounts(SOME_YOLT_USER_ID)).willReturn(
                Collections.singletonList(CreditScoreAccountDTO.builder()
                        .id(SOME_YOLT_USER_ACCOUNT_ID)
                        .accountReference(AccountReference.builder()
                                .iban("SOME_IBAN")
                                .build())
                        .build()));
        given(userStorageService.updateAccountForUser(SOME_USER_ID, SOME_YOLT_USER_ACCOUNT_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .selectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                        .build()
        );
        // When
        var user = siteConnectionUseCase.updateAccountForUser(SOME_USER_ID, SOME_YOLT_USER_ACCOUNT_ID);

        // Then
        assertThat(user).hasFieldOrPropertyWithValue("selectedAccountId", SOME_YOLT_USER_ACCOUNT_ID);
    }

}
