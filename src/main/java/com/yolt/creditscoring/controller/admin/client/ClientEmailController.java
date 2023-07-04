package com.yolt.creditscoring.controller.admin.client;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.client.ClientStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
@RestController
@RequiredArgsConstructor
public class ClientEmailController {

    public static final String CLIENT_TEMPLATE_ENDPOINT = "/api/admin/client/template";
    private final ClientStorageService clientService;

    @GetMapping(CLIENT_TEMPLATE_ENDPOINT)
    public List<ClientEmailDTO> getAllClientTemplates(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return clientService.getAllClientEmailTemplates(principal.getClientId());
    }
}
