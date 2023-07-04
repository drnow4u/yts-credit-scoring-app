package com.yolt.creditscoring.controller.admin.clienttoken;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.service.clienttoken.ClientTokenUseCase;
import com.yolt.creditscoring.service.clienttoken.CreateClientTokenRequestDTO;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokensDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class ClientTokenController {

    public static final String CREATE_TOKEN_ENDPOINT = "/api/admin/token";
    public static final String LIST_TOKEN_ENDPOINT = "/api/admin/token";
    public static final String LIST_TOKEN_PERMISSIONS_ENDPOINT = "/api/admin/token/permissions";
    public static final String REVOKE_TOKEN_ENDPOINT = "/api/admin/token/{jwtID}";

    private final ClientTokenUseCase clientTokenUseCase;

    @PostMapping(CREATE_TOKEN_ENDPOINT)
    public @Valid ClientTokenResponse createToken(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal,
                                                  @Valid @RequestBody CreateClientTokenRequestDTO createClientTokenRequestDTO) {
        return new ClientTokenResponse(
                clientTokenUseCase.createClientToken(
                        principal.getClientId(),
                        principal.getAdminId(),
                        principal.getEmail(),
                        createClientTokenRequestDTO
                )
        );
    }

    public record ClientTokenResponse(@NotNull String clientToken) {
    }

    @GetMapping(LIST_TOKEN_ENDPOINT)
    public List<@Valid ClientTokensDTO> listTokens(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return clientTokenUseCase.getAllClientTokensForClient(principal.getClientId());
    }

    @GetMapping(LIST_TOKEN_PERMISSIONS_ENDPOINT)
    public List<ClientTokenPermission> listTokenPermissions(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return clientTokenUseCase.getAllAvailableClientTokenPermissions(principal.getClientId());
    }

    @PutMapping(REVOKE_TOKEN_ENDPOINT)
    public void revokeToken(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal, @PathVariable UUID jwtID) {
        clientTokenUseCase.revokeClientToken(principal.getClientId(), jwtID);
    }
}
