package com.yolt.creditscoring.controller.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.AdminClaims;
import lombok.RequiredArgsConstructor;
import org.jose4j.jwt.JwtClaims;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Secured({SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN, SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN})
public class AdminController {

    /**
     * This endpoint exposes the user that is logged in.
     * This is, for now, primarily used by the frontend so the frontend can know the roles of the user that is logged in.
     * <p>
     * We intentionally inject a 'jwtClaims' principal here, because it could either be a client-admin, a cfa-admin,
     * or both.
     */
    @GetMapping("/api/admin/me")
    public @Valid AdminDTO account(@AuthenticationPrincipal JwtClaims jwtClaims, Authentication authentication) {

        return new AdminDTO((String) jwtClaims.getClaimValue(AdminClaims.EMAIL), (String) jwtClaims.getClaimValue(AdminClaims.IPDID),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()));
    }

    record AdminDTO(String email, String idpId, Set<String> roles) {
    }
}
