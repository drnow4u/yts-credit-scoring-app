package com.yolt.creditscoring.service.securitymodule.semaevent;

import com.yolt.creditscoring.configuration.security.admin.OAuth2AdminUser;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemaEventService {

    public void logIncorrectSignatureFromExternalSystem(InvalidSignatureDTO invalidSignatureDTO, UUID userId, UUID clientId) {
        var invalidSignatureSemaEvent = InvalidSignatureSemaEvent.builder()
                .message("Verification of report signature failed on the frontend application")
                .alarmTriggeredBy(userId)
                .clientId(clientId)
                .signature(invalidSignatureDTO.getSignature().toString())
                .userId(invalidSignatureDTO.getUserId())
                .build();
        SemaEventLogger.log(invalidSignatureSemaEvent);
        log.error("Verification of report signature failed on the frontend application for user {}", invalidSignatureDTO.getUserId());
    }

    public void logIncorrectSignature(InvalidSignatureDTO invalidSignatureDTO, UUID userId, UUID clientId) {
        var invalidSignatureSemaEvent = InvalidSignatureSemaEvent.builder()
                .message("Verification of report signature")
                .alarmTriggeredBy(userId)
                .clientId(clientId)
                .signature(invalidSignatureDTO.getSignature().toString())
                .userId(invalidSignatureDTO.getUserId())
                .build();
        SemaEventLogger.log(invalidSignatureSemaEvent);
        log.error("Verification of report signature failed for user {}", invalidSignatureDTO.getUserId());
    }

    public void logIncorrectSignaturePublicKey(UUID kid) {
        var invalidSignatureSemaEvent = InvalidSignatureSemaEvent.builder()
                .message("Signature public key thumbprint in Secret Pipeline differ then stored in database for the same key ID: " + kid.toString())
                .build();
        SemaEventLogger.log(invalidSignatureSemaEvent);
        log.error("Signature public key thumbprint in Secret Pipeline differ then stored in database for the same key ID: {}", kid);
    }

    public void logAdminLoginToApplication(OAuth2AdminUser principal) {
        SemaEventLogger.log(AdminLoginSemaEvent.builder()
                .clientId(principal.getClientAdmin().map(ClientAdmin::getClientId).orElse(null))
                .idpId(principal.getIdpId())
                .adminEmail(principal.getEmail())
                .build());
    }

    public void logUserInvitation(UUID clientId, UUID adminId) {
        SemaEventLogger.log(InvitationSpikesSemaEvent.builder()
                .clientId(clientId)
                .adminId(adminId)
                .build());
    }

    public void logNotMatchingAdminEmailWithIdpId(UUID clientId, String idpId, String storedEmail, String responseEmail, String provider) {
        SemaEventLogger.log(AdminEmailDoesNotMatchIdpIdEvent.builder()
                .clientId(clientId)
                .idpId(idpId)
                .storedEmail(storedEmail)
                .responseEmail(responseEmail)
                .provider(provider)
                .build());
    }

    public void logClientTokenAccessToUnauthorizedEndpoint(UUID clientId, String endpointURI, List<? extends GrantedAuthority> permissions) {
        SemaEventLogger.log(ClientTokenAccessToUnauthorizedEndpointEvent.builder()
                .clientId(clientId)
                .endpointURI(endpointURI)
                .permissions(permissions)
                .build());
    }

    public void logNotRegisteredAdminLogin(String idpId, String provider, String ipAddress) {
        SemaEventLogger.log(NotRegisteredAdminLoginSemaEvent.builder()
                .idpId(idpId)
                .provider(provider)
                .ipAddress(ipAddress)
                .build());
    }
}
