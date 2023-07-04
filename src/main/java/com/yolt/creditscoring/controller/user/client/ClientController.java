package com.yolt.creditscoring.controller.user.client;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.service.client.ClientDTO;
import com.yolt.creditscoring.service.client.ClientStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
public class ClientController {

    public static final String CLIENT_ENDPOINT = "/api/user/client";
    private final ClientStorageService clientService;

    @GetMapping(CLIENT_ENDPOINT)
    public ClientDTO getClient(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return clientService.getClientInformationBasedOnLoggedUser(principal.getClientId());
    }
}
