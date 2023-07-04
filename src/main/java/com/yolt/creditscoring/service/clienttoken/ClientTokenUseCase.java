package com.yolt.creditscoring.service.clienttoken;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.exception.ClientTokenException;
import com.yolt.creditscoring.exception.ClientTokenNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientFeatureDisabledException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.clienttoken.model.*;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtClientToken;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class ClientTokenUseCase {

    private static final Clock clock = ClockConfig.getClock();
    private static final int ACTIVE_TOKENS_COUNT = 4;

    private final ClientTokenRepository clientTokenRepository;
    private final JwtCreationService jwtCreationService;
    private final AdminAuditService adminAuditService;
    private final ClientStorageService clientStorageService;

    public String createClientToken(@NonNull UUID clientId,
                                    @NonNull UUID adminId,
                                    @NonNull String adminEmail,
                                    @NonNull CreateClientTokenRequestDTO clientTokenRequestDTO) {

        if(!clientStorageService.hasApiTokenFeature(clientId)) {
            throw new ClientFeatureDisabledException("Client with Id " + clientId + " does not have api token feature");
        }

        if (ACTIVE_TOKENS_COUNT < clientTokenRepository.countByClientIdAndStatus(clientId, ClientTokenStatus.ACTIVE)) {
            throw new TooManyTokensException("Client with Id " + clientId + " has too many active tokens");
        }

        JwtClientToken jwtClientToken = jwtCreationService.createClientToken(clientTokenRequestDTO.permissions());

        OffsetDateTime createdDate = OffsetDateTime.now(clock);
        OffsetDateTime expirationDate = createdDate.plusDays(180);
        ClientTokenEntity clientTokenEntity = ClientTokenEntity.builder()
                .jwtId(jwtClientToken.jwtId())
                .signedPublicKeyId(jwtClientToken.publicKeyIdForVerification())
                .clientId(clientId)
                .name(clientTokenRequestDTO.name())
                .createdAdminEmail(adminEmail)
                .status(ClientTokenStatus.ACTIVE)
                .permissions(clientTokenRequestDTO.permissions())
                .createdDate(createdDate)
                .expirationDate(expirationDate)
                .build();

        clientTokenRepository.save(clientTokenEntity);

        adminAuditService.adminCreatedClientToken(clientId, adminId, adminEmail, createdDate, clientTokenRequestDTO.permissions());

        return jwtClientToken.encryptedJwt();
    }

    public List<@Valid ClientTokensDTO> getAllClientTokensForClient(@NonNull UUID clientId) {
        if(!clientStorageService.hasApiTokenFeature(clientId)) {
            throw new ClientFeatureDisabledException("Client with Id " + clientId + " does not have api token feature");
        }

        return clientTokenRepository.findAllByClientId(clientId).stream()
                .map(clientToken -> ClientTokensDTO.builder()
                        .id(clientToken.getJwtId())
                        .name(clientToken.getName())
                        .creationDate(clientToken.getCreatedDate())
                        .expiryDate(clientToken.getExpirationDate())
                        .lastUsed(clientToken.getLastAccessedDate())
                        .status(clientToken.getStatus())
                        .build())
                .toList();
    }

    public List<ClientTokenPermission> getAllAvailableClientTokenPermissions(@NonNull UUID clientId) {
        if(!clientStorageService.hasApiTokenFeature(clientId)) {
            throw new ClientFeatureDisabledException("Client with Id " + clientId + " does not have api token feature");
        }

        return Arrays.asList(ClientTokenPermission.values());
    }

    public void revokeClientToken(@NonNull UUID clientId, @NonNull UUID jwtID) {
        if(!clientStorageService.hasApiTokenFeature(clientId)) {
            throw new ClientFeatureDisabledException("Client with Id " + clientId + " does not have api token feature");
        }

        ClientTokenEntity clientTokenEntity = clientTokenRepository.findById(jwtID)
                .orElseThrow(() -> new ClientTokenNotFoundException("Client token not found for revoking"));

        if (!clientTokenEntity.getClientId().equals(clientId)) {
            throw new ClientTokenNotFoundException("Client Token ID does not match with in JWT");
        }

        clientTokenEntity.setStatus(ClientTokenStatus.REVOKED);

        clientTokenRepository.save(clientTokenEntity);
    }
}
