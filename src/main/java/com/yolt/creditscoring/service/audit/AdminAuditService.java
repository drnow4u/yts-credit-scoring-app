package com.yolt.creditscoring.service.audit;

import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import com.yolt.creditscoring.configuration.security.admin.OAuth2AdminUser;
import com.yolt.creditscoring.exception.OAuth2NotRegisteredAdminException;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import lombok.NonNull;
import nl.ing.lovebird.logging.AuditLogger;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminAuditService {

    private static final String USER_ID_FIELD = "userId";

    public void adminLogIn(OAuth2AdminUser oAuth2AdminUser, String adminIpAddress, String userAgent) {
        AuditLogger.logSuccess("Cashflow Analyser client admin login", AdminAuditDTO.builder()
                .adminEmail(oAuth2AdminUser.getEmail())
                .idpId(oAuth2AdminUser.getIdpId())
                .clientId(oAuth2AdminUser.getClientAdmin().map(ClientAdmin::getClientId).orElse(null))
                .adminId(oAuth2AdminUser.getClientAdmin().map(ClientAdmin::getId).orElse(null))
                .detail("ipAddress", adminIpAddress)
                .detail("userAgent", userAgent)
                .build());
    }

    public void inviteNewUser(@NonNull UUID clientId, @NonNull UUID adminId, @NonNull String adminEmail,
                              @NonNull UUID userId, @NonNull String userName, @NonNull String userEmail,
                              @NonNull ClientAccessType clientAccessType) {
        AuditLogger.logSuccess("Cashflow Analyser client invited user, access type: " + clientAccessType, ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .clientAccessType(clientAccessType)
                .detail(USER_ID_FIELD, userId.toString())
                .detail("userName", userName)
                .detail("userEmail", userEmail)
                .build());
    }

    public void reinviteUser(UUID clientId, UUID adminId, String adminEmail, UUID userId, String userName, String userEmail) {
        AuditLogger.logSuccess("Cashflow Analyser client admin re-invited user", ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .detail(USER_ID_FIELD, userId.toString())
                .detail("userName", userName)
                .detail("userEmail", userEmail)
                .build());
    }

    public void deleteUser(UUID clientId, UUID adminId, String adminEmail, UUID userId, String userEmail,
                           @NonNull ClientAccessType clientAccessType) {

        AuditLogger.logSuccess("Cashflow Analyser client deleted user", ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .clientAccessType(clientAccessType)
                .detail(USER_ID_FIELD, userId.toString())
                .detail("userEmail", userEmail)
                .build());
    }

    public void adminViewedCreditReport(UUID clientId, UUID adminId, String adminEmail, UUID userId) {
        AuditLogger.logSuccess("Cashflow Analyser client admin viewed user report", ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .detail(USER_ID_FIELD, userId.toString())
                .build());
    }

    public void adminCreatedClientToken(UUID clientId, UUID adminId, String adminEmail, OffsetDateTime createdDate, List<ClientTokenPermission> permissions) {
        AuditLogger.logSuccess("Cashflow Analyser client admin created client token", ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .detail("createdDate", createdDate.toString())
                .detail("permissions", String.join(",", permissions.stream().map(Enum::name).collect(Collectors.joining(","))))
                .build());
    }

    public void adminFetchCreditReport(UUID clientId, UUID adminId, String adminEmail, UUID userId) {
        AuditLogger.logSuccess("Cashflow Analyser client admin fetch user report via API", ClientAdminAuditDTO.builder()
                .clientId(clientId)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .detail(USER_ID_FIELD, userId.toString())
                .build());
    }

    public void adminNotRegisteredLogIn(String idpId, String provider, OAuth2NotRegisteredAdminException exception) {
        AuditLogger.logError("Cashflow Analyser client admin fetch user report via API",
                AdminNotRegisteredLogInDTO.builder()
                        .idpId(idpId)
                        .provider(provider),
                exception);
    }
}
