package com.yolt.creditscoring.controller.admin.account;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.service.client.ClientDTO;
import com.yolt.creditscoring.service.client.ClientSettingsDTO;
import com.yolt.creditscoring.service.client.ClientStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class ClientAdminController {

    public static final String ACCOUNT_ENDPOINT = "/api/admin/account";

    private final ClientStorageService clientStorageService;

    @GetMapping(ACCOUNT_ENDPOINT)
    public @Valid ClientAdminAuthDTO account(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        ClientDTO clientDTO = clientStorageService.getClientInformationBasedOnLoggedUser(principal.getClientId());
        ClientSettingsDTO clientSettingsDTO = clientStorageService.getClientSettings(principal.getClientId());

        return ClientAdminAuthDTO.builder()
                .email(principal.getEmail())
                .name(clientDTO.getName())
                .clientSettings(clientSettingsDTO)
                .build();
    }

}
