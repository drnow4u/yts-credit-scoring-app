package com.yolt.creditscoring.service.clienttoken;

import com.yolt.creditscoring.exception.ClientTokenException;
import com.yolt.creditscoring.exception.ClientTokenNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientFeatureDisabledException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtClientToken;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ClientTokenUseCaseTest {

    @Mock
    private ClientTokenRepository clientTokenRepository;

    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private ClientStorageService clientStorageService;

    @InjectMocks
    private ClientTokenUseCase clientTokenUseCase;

    @Test
    void shouldCorrectlyGenerateClientToken() {
        //Given
        ArgumentCaptor<ClientTokenEntity> clientTokenEntityArgumentCaptor = ArgumentCaptor.forClass(ClientTokenEntity.class);

        List<ClientTokenPermission> givenListOfPermissions = List.of(ClientTokenPermission.INVITE_USER, ClientTokenPermission.DOWNLOAD_REPORT);

        given(clientStorageService.hasApiTokenFeature(SOME_CLIENT_ID)).willReturn(true);
        given(clientTokenRepository.countByClientIdAndStatus(SOME_CLIENT_ID, ClientTokenStatus.ACTIVE)).willReturn(2L);

        given(jwtCreationService.createClientToken(givenListOfPermissions))
                .willReturn(new JwtClientToken(SOME_CLIENT_JWT_ID, SOME_CLIENT_ENCRYPTED_JWT, SOME_JWT_PUBLIC_KEY_ID));

        //When
        String result = clientTokenUseCase.createClientToken(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, new CreateClientTokenRequestDTO(SOME_CLIENT_JWT_NAME, givenListOfPermissions));

        //Then
        assertThat(result).isEqualTo(SOME_CLIENT_ENCRYPTED_JWT);

        then(clientTokenRepository).should().save(clientTokenEntityArgumentCaptor.capture());

        ClientTokenEntity savedClientToken = clientTokenEntityArgumentCaptor.getValue();
        assertThat(savedClientToken.getJwtId()).isEqualTo(SOME_CLIENT_JWT_ID);
        assertThat(savedClientToken.getClientId()).isEqualTo(SOME_CLIENT_ID);
        assertThat(savedClientToken.getName()).isEqualTo(SOME_CLIENT_JWT_NAME);
        assertThat(savedClientToken.getCreatedAdminEmail()).isEqualTo(SOME_CLIENT_ADMIN_EMAIL);
        assertThat(savedClientToken.getCreatedDate()).isNotNull();
        assertThat(savedClientToken.getExpirationDate()).isEqualTo(savedClientToken.getCreatedDate().plusDays(180));
        assertThat(savedClientToken.getLastAccessedDate()).isNull();
        assertThat(savedClientToken.getStatus()).isEqualTo(ClientTokenStatus.ACTIVE);
        assertThat(savedClientToken.getPermissions()).isEqualTo(givenListOfPermissions);

        then(adminAuditService).should().adminCreatedClientToken(
                eq(SOME_CLIENT_ID), eq(SOME_CLIENT_ADMIN_ID), eq(SOME_CLIENT_ADMIN_EMAIL), any(OffsetDateTime.class), eq(givenListOfPermissions));
    }

    @Test
    void shouldThrownExceptionWhenActiveLimitOfTokenWillExtend() {
        //Given
        List<ClientTokenPermission> givenListOfPermissions = List.of(ClientTokenPermission.INVITE_USER, ClientTokenPermission.DOWNLOAD_REPORT);

        given(clientStorageService.hasApiTokenFeature(SOME_CLIENT_ID)).willReturn(true);
        given(clientTokenRepository.countByClientIdAndStatus(SOME_CLIENT_ID, ClientTokenStatus.ACTIVE)).willReturn(5L);

        // When
        Throwable thrown = catchThrowable(() -> clientTokenUseCase.createClientToken(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, new CreateClientTokenRequestDTO(SOME_CLIENT_JWT_NAME, givenListOfPermissions)));

        // Then
        assertThat(thrown).isInstanceOf(TooManyTokensException.class);
        then(clientTokenRepository).should(never()).save(any(ClientTokenEntity.class));
        then(adminAuditService).should(never()).adminCreatedClientToken(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrownExceptionWhenCreatingTokenAndFeatureIsOffOnClient() {
        //Given
        List<ClientTokenPermission> givenListOfPermissions = List.of(ClientTokenPermission.INVITE_USER, ClientTokenPermission.DOWNLOAD_REPORT);

        given(clientStorageService.hasApiTokenFeature(SOME_CLIENT_ID)).willReturn(false);

        // When
        Throwable thrown = catchThrowable(() -> clientTokenUseCase.createClientToken(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, new CreateClientTokenRequestDTO(SOME_CLIENT_JWT_NAME, givenListOfPermissions)));

        // Then
        assertThat(thrown).isInstanceOf(ClientFeatureDisabledException.class);
        then(clientTokenRepository).should(never()).save(any(ClientTokenEntity.class));
        then(adminAuditService).should(never()).adminCreatedClientToken(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrownExceptionWhenTryingToRevokeTokenWithoutFeatureToggle() {
        // Given
        given(clientStorageService.hasApiTokenFeature(SOME_CLIENT_ID)).willReturn(false);

        // When
        Throwable thrown = catchThrowable(() -> clientTokenUseCase.revokeClientToken(SOME_CLIENT_ID, SOME_CLIENT_JWT_ID));

        // Then
        assertThat(thrown).isInstanceOf(ClientFeatureDisabledException.class);
        then(clientTokenRepository).should(never()).save(any(ClientTokenEntity.class));
    }


    @Test
    void shouldThrownExceptionWhenTryingToRevokeTokenAndClientIdDoesNotMatch() {
        // Given
        ClientTokenEntity clientToken = ClientTokenEntity.builder()
                .jwtId(UUID.fromString("a2ecf9a2-85c4-47b7-bd83-54c3ee28c2df"))
                .name("First Token")
                .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                .clientId(SOME_CLIENT_ID_2)
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .createdDate(SOME_TEST_DATE)
                .expirationDate(SOME_TEST_DATE.plusDays(1))
                .permissions(List.of(ClientTokenPermission.INVITE_USER))
                .build();

        given(clientStorageService.hasApiTokenFeature(SOME_CLIENT_ID)).willReturn(true);
        given(clientTokenRepository.findById(SOME_CLIENT_JWT_ID)).willReturn(Optional.of(clientToken));

        // When
        Throwable thrown = catchThrowable(() -> clientTokenUseCase.revokeClientToken(SOME_CLIENT_ID, SOME_CLIENT_JWT_ID));

        // Then
        assertThat(thrown).isInstanceOf(ClientTokenNotFoundException.class);
        then(clientTokenRepository).should(never()).save(any(ClientTokenEntity.class));
    }
}