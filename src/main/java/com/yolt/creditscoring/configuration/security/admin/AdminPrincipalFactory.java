package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.JwtClaims;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AdminPrincipalFactory {

    public ClientAdminPrincipal createClientAdmin(JwtClaims jwtClaims) {
        String adminIdClaim = (String) jwtClaims.getClaimValue(AdminClaims.ADMIN_ID);
        String clientIdClaim = (String) jwtClaims.getClaimValue(AdminClaims.CLIENT_ID);
        String email = (String) jwtClaims.getClaimValue(AdminClaims.EMAIL);
        String ipdId = (String) jwtClaims.getClaimValue(AdminClaims.IPDID);

        List<String> roles = (List<String>) jwtClaims.getClaimValue(AdminClaims.ROLES);
        if (adminIdClaim == null || clientIdClaim == null || email == null || ipdId == null ||
                roles == null || !roles.contains(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)) {
            log.info("Unable to create Client admin principal for admin Id {}, with roles: {}", adminIdClaim, roles);
            return null;
        }

        return new ClientAdminPrincipal(
                UUID.fromString(adminIdClaim),
                UUID.fromString(clientIdClaim),
                email,
                ipdId
        );
    }

    public CfaAdminPrincipal createAdminPrincipal(JwtClaims jwtClaims) {
        String email = (String) jwtClaims.getClaimValue(AdminClaims.EMAIL);
        String ipdId = (String) jwtClaims.getClaimValue(AdminClaims.IPDID);
        List<String> roles = (List<String>) jwtClaims.getClaimValue(AdminClaims.ROLES);
        if (email == null || ipdId == null ||
                roles == null || !roles.contains(SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN)) {
            log.info("Unable to create CFA admin principal for IpdId {}, with roles: {}", ipdId, roles);
            return null;
        }
        return new CfaAdminPrincipal(email, ipdId);

    }

}


