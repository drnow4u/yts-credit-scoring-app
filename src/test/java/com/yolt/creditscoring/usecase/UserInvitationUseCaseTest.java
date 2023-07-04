package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.controller.user.invitation.ConsentViewDTO;
import com.yolt.creditscoring.exception.InvalidStatusException;
import com.yolt.creditscoring.exception.InvitationExpiredException;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.legaldocument.LegalDocumentService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInvitationUseCaseTest {

    @Mock
    private LegalDocumentService consentService;

    @Mock
    private YoltProvider yoltProvider;

    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private UserAuditService userAuditService;

    @Mock
    CreditScoreUserRepository creditScoreUserRepository;

    private UserInvitationUseCase userInvitationUseCase;

    @BeforeEach
    void setUp() {
        UserStorageService userStorageService = new UserStorageService(creditScoreUserRepository);

        userInvitationUseCase = new UserInvitationUseCase(
                consentService,
                yoltProvider,
                jwtCreationService,
                userStorageService,
                userJourneyService,
                userAuditService);
    }

    @Test
    void shouldValidateUserWithCorrectHash() {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.INVITED)
                .setDateTimeInvited(SOME_STILL_VALID_INVITATION_DATE);

        when(creditScoreUserRepository.findByInvitationHash(SOME_USER_HASH)).thenReturn(Optional.of(user));
        when(jwtCreationService.createUserToken(SOME_USER_HASH)).thenReturn(SOME_USER_JWT);

        // When
        ConsentViewDTO consentViewDTO = userInvitationUseCase.validateUser(SOME_USER_HASH);

        // Then
        assertThat(consentViewDTO.getToken()).isEqualTo(SOME_USER_JWT);
        then(userJourneyService).should(never()).registerConsentGenerated(any(), any());
    }

    @Test
    void shouldThrowInvitationExpiredExceptionWhenHashExpired() {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.INVITED)
                .setDateTimeInvited(SOME_NOT_VALID_INVITATION_DATE);

        when(creditScoreUserRepository.findByInvitationHash(SOME_USER_HASH)).thenReturn(Optional.of(user));
        when(creditScoreUserRepository.findById(SOME_USER_ID)).thenReturn(Optional.of(user));
        when(creditScoreUserRepository.save(any())).thenReturn(user);

        // When
        Throwable thrown = catchThrowable(() -> userInvitationUseCase.validateUser(SOME_USER_HASH));

        // Then
        assertThat(thrown).isInstanceOf(InvitationExpiredException.class)
                .hasMessageContaining("The invitation link have expired");

        then(userJourneyService).should(never()).registerConsentGenerated(any(), any());
    }

    @Test
    void shouldSaveUserConsent() {
        // Given
        CreditScoreUser user = getCreditScoreUser();

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(user));
        given(creditScoreUserRepository.save(any())).willReturn(user);

        given((consentService.getCurrentTermsAndConditions())).willReturn(SOME_T_AND_C);
        given((consentService.getCurrentPrivacyPolicy())).willReturn(SOME_PRIVACY_POLICY);
        given(yoltProvider.createUser()).willReturn(SOME_YOLT_USER_ID);

        // When
        userInvitationUseCase.saveLoggedUserConsent(SOME_USER_ID, true, SOME_USER_AGENT, SOME_USER_IP);

        // Then
        ArgumentCaptor<CreditScoreUser> savedUserCaptor = ArgumentCaptor.forClass(CreditScoreUser.class);
        then(creditScoreUserRepository).should().save(savedUserCaptor.capture());
        assertThatSomeUserIsEqualTo(savedUserCaptor.getValue());

        then(userStorageService).should(never()).saveUserConsentDecline(SOME_USER_ID);
        then(userAuditService).should().logUserConsentInAuditLog(any(), eq(SOME_USER_EMAIL), eq(SOME_CLIENT_ID));
        then(userAuditService).should().logUserConsentInAuditLog(any(), any(), any());

        then(userJourneyService).should().registerConsentGenerated(SOME_CLIENT_ID, SOME_USER_ID);
    }

    private void assertThatSomeUserIsEqualTo(CreditScoreUser savedUser) {
        assertThat(savedUser.getId()).isEqualTo(SOME_USER_ID);
        assertThat(savedUser.getDateTimeConsent()).isNotNull();
        assertThat(savedUser.getTermsAndConditionId()).isEqualTo(SOME_T_AND_C.getId());
        assertThat(savedUser.getPrivacyPolicyId()).isEqualTo(SOME_PRIVACY_POLICY.getId());
        assertThat(savedUser.getUserAgent()).isEqualTo(SOME_USER_AGENT);
        assertThat(savedUser.getIpAddress()).isEqualTo(SOME_USER_IP);
        assertThat(savedUser.getYoltUserId()).isEqualTo(SOME_YOLT_USER_ID);
    }

    @Test
    void shouldSaveUserConsentOnlyOnce() {
        // Given
        CreditScoreUser user = getCreditScoreUser();
        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(user));
        given((consentService.getCurrentTermsAndConditions())).willReturn(SOME_T_AND_C);
        given((consentService.getCurrentPrivacyPolicy())).willReturn(SOME_PRIVACY_POLICY);
        given(yoltProvider.createUser()).willReturn(SOME_YOLT_USER_ID);
        given(userJourneyService.isConsentGeneratedRegistered(SOME_CLIENT_ID, SOME_USER_ID)).willReturn(true);

        userInvitationUseCase.saveLoggedUserConsent(SOME_USER_ID, true, SOME_USER_AGENT, SOME_USER_IP);

        then(creditScoreUserRepository).should(atMost(2)).findById(SOME_USER_ID);
        then(creditScoreUserRepository).shouldHaveNoMoreInteractions();
        then(userJourneyService).should().isConsentGeneratedRegistered(SOME_CLIENT_ID, SOME_USER_ID);
        then(userJourneyService).shouldHaveNoMoreInteractions();

        then(userAuditService).should().logUserConsentInAuditLog(any(), eq(SOME_USER_EMAIL), eq(SOME_CLIENT_ID));
        then(userAuditService).should().logUserConsentInAuditLog(any(), any(), any());
        then(userStorageService).should(never()).saveUserConsent(any());
        then(userJourneyService).should(never()).registerConsentGenerated(SOME_CLIENT_ID, SOME_USER_ID);
    }

    private CreditScoreUser getCreditScoreUser() {
        return new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setDateTimeInvited(SOME_STILL_VALID_INVITATION_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setEmail(SOME_USER_EMAIL);
    }

    @Test
    void shouldNotCreateYoltUserWhenOneAlreadyExists() {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setDateTimeInvited(SOME_STILL_VALID_INVITATION_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setEmail(SOME_USER_EMAIL)
                .setYoltUserId(SOME_YOLT_USER_ID);

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(user));
        given(creditScoreUserRepository.save(any())).willReturn(user);

        given(consentService.getCurrentTermsAndConditions()).willReturn(SOME_T_AND_C);
        given(consentService.getCurrentPrivacyPolicy()).willReturn(SOME_PRIVACY_POLICY);

        // When
        userInvitationUseCase.saveLoggedUserConsent(SOME_USER_ID, true, SOME_USER_AGENT, SOME_USER_IP);

        // Then
        ArgumentCaptor<CreditScoreUser> savedUserCaptor = ArgumentCaptor.forClass(CreditScoreUser.class);
        then(creditScoreUserRepository).should().save(savedUserCaptor.capture());
        then(yoltProvider).should(never()).createUser();
        CreditScoreUser savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(SOME_USER_ID);
        assertThat(savedUser.getTermsAndConditionId()).isEqualTo(SOME_T_AND_C.getId());
        assertThat(savedUser.getUserAgent()).isEqualTo(SOME_USER_AGENT);
        assertThat(savedUser.getIpAddress()).isEqualTo(SOME_USER_IP);
        assertThat(savedUser.getYoltUserId()).isEqualTo(SOME_YOLT_USER_ID);

        then(userJourneyService).should().registerConsentGenerated(SOME_CLIENT_ID, SOME_USER_ID);
    }

    @Test
    void shouldSaveWhenUserDidNotConsent() {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.INVITED)
                .setEmail(SOME_USER_EMAIL);

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(user));
        given(creditScoreUserRepository.save(any())).willReturn(user);

        // When
        userInvitationUseCase.saveLoggedUserConsent(SOME_USER_ID, false, SOME_USER_AGENT, SOME_USER_IP);

        // Then
        ArgumentCaptor<CreditScoreUser> savedUserCaptor = ArgumentCaptor.forClass(CreditScoreUser.class);
        then(creditScoreUserRepository).should().save(savedUserCaptor.capture());
        CreditScoreUser savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(SOME_USER_ID);
        assertThat(savedUser.isConsent()).isFalse();

        then(consentService).should(never()).getCurrentTermsAndConditions();
        then(yoltProvider).should(never()).createUser();
        then(userJourneyService).should(never()).registerConsentGenerated(any(), any());
    }

    @ParameterizedTest
    @MethodSource("invitationStatuesForInvalidFlow")
    void shouldNotSaveUserConsentWhenInvitationStatusIsCompletedOrExpired(boolean isConsent, InvitationStatus status, String errorMessage) {
        // Given
        CreditScoreUser user = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(status)
                .setEmail(SOME_USER_EMAIL);

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(user));

        // When
        Throwable thrown = catchThrowable(
                () -> userInvitationUseCase.saveLoggedUserConsent(SOME_USER_ID, isConsent, SOME_USER_AGENT, SOME_USER_IP));

        // Then
        assertThat(thrown).isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("Invalid user status for: " + status + " For " + errorMessage);

        then(userJourneyService).should(never()).registerConsentGenerated(any(), any());
    }

    private static Stream<Arguments> invitationStatuesForInvalidFlow() {
        return Stream.of(
                Arguments.of(true, InvitationStatus.EXPIRED, "accepting consent"),
                Arguments.of(true, InvitationStatus.COMPLETED, "accepting consent"),
                Arguments.of(false, InvitationStatus.EXPIRED, "deny consent"),
                Arguments.of(false, InvitationStatus.COMPLETED, "deny consent")
        );
    }
}
