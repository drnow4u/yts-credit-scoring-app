package com.yolt.creditscoring.usecase;

import brave.baggage.BaggageField;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import com.yolt.creditscoring.controller.admin.users.InviteUserDTO;
import com.yolt.creditscoring.controller.admin.users.ViewUserDTO;
import com.yolt.creditscoring.exception.InviteStillPendingException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.email.EmailService;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.user.UserDiagnosticContextFilter.APP_USER_ID_FIELD_NAME;
import static nl.ing.lovebird.logging.MDCContextCreator.CLIENT_USER_ID_HEADER_NAME;
import static nl.ing.lovebird.logging.MDCContextCreator.USER_SITE_ID_MDC_KEY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserManagementUseCaseTest {

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private EmailService emailService;

    @Mock
    private ClientStorageService clientService;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private YoltProvider yoltProvider;

    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private SemaEventService semaEventService;

    private UserManagementUseCase userManagementUseCase;

    @Mock
    CreditScoreUserRepository creditScoreUserRepository;


    @BeforeEach
    void setUp() {
        UserStorageService userStorageService = new UserStorageService(creditScoreUserRepository);

        userManagementUseCase = new UserManagementUseCase(userStorageService,
                emailService,
                clientService,
                userJourneyService,
                yoltProvider,
                adminAuditService,
                semaEventService,
                () -> "fixed-invitation-hash",
                BaggageField.create(CLIENT_USER_ID_HEADER_NAME),
                BaggageField.create(USER_SITE_ID_MDC_KEY),
                BaggageField.create(APP_USER_ID_FIELD_NAME)

        );
    }

    @Test
    void shouldInviteNewUser() {
        // Given
        InviteUserDTO inviteUserDTO = new InviteUserDTO(SOME_USER_NAME, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID);

        ClientEmailDTO clientEmail = ClientEmailDTO.builder()
                .id(SOME_CLIENT_EMAIL_ID)
                .template("UserInvitation_Test_Client")
                .subject("Hello!")
                .sender("Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>")
                .build();

        CreditScoreUser createdUser = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setClientId(SOME_CLIENT_ID)
                .setStatus(InvitationStatus.INVITED);

        InvitationEmailData invitationEmailData = InvitationEmailData.builder()
                .clientEmail(clientEmail)
                .recipientEmail(createdUser.getEmail())
                .userName(SOME_USER_NAME)
                .clientLogoUrl("http://localhost/app-context/clients/" + SOME_CLIENT_ID + "/logo")
                .redirectUrl("http://localhost/app-context/consent/fixed-invitation-hash")
                .build();

        given(clientService.getClientEmailById(SOME_CLIENT_EMAIL_ID)).willReturn(clientEmail);
        given(creditScoreUserRepository.save(any())).willReturn(createdUser);

        // When
        UUID userId = userManagementUseCase.inviteNewUser(
                inviteUserDTO,
                SOME_CLIENT_ID,
                "http://localhost/app-context",
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL,
                ClientAccessType.ADMIN);

        // Then
        assertThat(userId).isEqualTo(SOME_USER_ID);

        then(emailService).should().sendInvitationForUser(createdUser.getClientId(), createdUser.getId(), invitationEmailData);
        then(userJourneyService).should().registerInvited(eq(SOME_CLIENT_ID), any());
        then(semaEventService).should().logUserInvitation(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID);
        then(adminAuditService).should().inviteNewUser(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, SOME_USER_ID, SOME_USER_NAME, SOME_USER_EMAIL, ClientAccessType.ADMIN);
    }

    @Test
    void shouldReInviteUser() {
        // Given
        ArgumentCaptor<CreditScoreUser> savedCreditScoreUserCaptor = ArgumentCaptor.forClass(CreditScoreUser.class);

        final ClientEmailDTO clientEmail = ClientEmailDTO.builder()
                .id(SOME_CLIENT_EMAIL_ID)
                .template("UserInvitation_Test_Client")
                .subject("Hello!")
                .sender("Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>")
                .build();

        final CreditScoreUser creditScoreUser = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setClientEmailId(SOME_CLIENT_EMAIL_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setStatus(InvitationStatus.EXPIRED);

        final InvitationEmailData invitationEmailData = InvitationEmailData.builder()
                .clientEmail(clientEmail)
                .recipientEmail(creditScoreUser.getEmail())
                .userName(SOME_USER_NAME)
                .clientLogoUrl("http://localhost/app-context/clients/" + SOME_CLIENT_ID + "/logo")
                .redirectUrl("http://localhost/app-context/consent/fixed-invitation-hash")
                .build();

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(creditScoreUser));
        given(creditScoreUserRepository.save(any())).willReturn(creditScoreUser);
        given(clientService.getClientEmailById(SOME_CLIENT_EMAIL_ID)).willReturn(clientEmail);

        // When
        userManagementUseCase.resendUserInvite(
                SOME_USER_ID,
                "http://localhost/app-context",
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL);

        // Then
        then(creditScoreUserRepository).should().save(savedCreditScoreUserCaptor.capture());

        then(emailService).should().sendInvitationForUser(creditScoreUser.getClientId(), creditScoreUser.getId(), invitationEmailData);
        then(userJourneyService).should(never()).registerInvited(any(), any());
        then(adminAuditService).should().reinviteUser(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, SOME_USER_ID, SOME_USER_NAME, SOME_USER_EMAIL);
    }

    @ParameterizedTest
    @EnumSource(value = InvitationStatus.class, names = {"INVITED", "CALCULATION_ERROR", "REFUSED", "COMPLETED"})
    void shouldNotReInviteUserWhenInvitationStatusIsNotExpired(InvitationStatus invitationStatus) {
        // Given
        CreditScoreUser creditScoreUser = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID);
        given(creditScoreUserRepository.findById(SOME_USER_ID))
                .willReturn(Optional.of(creditScoreUser));

        // When
        Throwable thrown = catchThrowable(() -> userManagementUseCase.resendUserInvite(
                SOME_USER_ID,
                "http://localhost/app-context/",
                SOME_CLIENT_ID,
                SOME_CLIENT_ADMIN_ID,
                SOME_CLIENT_ADMIN_EMAIL));

        // Then
        assertThat(thrown).isInstanceOf(InviteStillPendingException.class);
        then(userStorageService)
                .should(never())
                .updateUserInvitationHashAndSetStatusInvited(any(), any());

        then(emailService).should(never()).sendInvitationForUser(any(), any(), any());

        then(userJourneyService).should(never()).registerInvited(any(), any());
    }

    @Test
    void shouldObtainUsersForGivenClient() {
        // Given
        CreditScoreUser creditScoreUser = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setDateTimeStatusChange(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setClientId(SOME_CLIENT_ID);

        Pageable pageable = mock(Pageable.class);

        given(creditScoreUserRepository.findByClientId(SOME_CLIENT_ID, pageable))
                .willReturn(new PageImpl<>(Collections.singletonList(creditScoreUser)));

        // When
        Page<ViewUserDTO> viewUserDTOs = userManagementUseCase.viewAllUsersByClientId(pageable, SOME_CLIENT_ID);

        // Then
        assertThat(viewUserDTOs).hasSize(1);
        assertThat(viewUserDTOs).extracting("email", "dateInvited", "dateStatusUpdated", "status")
                .contains(tuple(SOME_USER_EMAIL, SOME_TEST_DATE, SOME_TEST_DATE, InvitationStatus.INVITED));

        then(userJourneyService).should(never()).registerInvited(any(), any());
    }

    @Test
    void shouldRemoveUserForGivenClient() {
        // Given
        CreditScoreUser creditScoreUser = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setEmail(SOME_USER_EMAIL);
        ClientAccessType accessType = ClientAccessType.ADMIN;

        given(creditScoreUserRepository.findById(SOME_USER_ID)).willReturn(Optional.of(creditScoreUser));

        // When
        userManagementUseCase.deleteUserByUserID(SOME_USER_ID, SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, accessType);

        // Then
        then(creditScoreUserRepository).should().deleteById(SOME_USER_ID);

        then(userJourneyService).should(never()).registerInvited(any(), any());

        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);

        then(adminAuditService).should().deleteUser(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, SOME_USER_ID, SOME_USER_EMAIL, accessType);
    }

}
