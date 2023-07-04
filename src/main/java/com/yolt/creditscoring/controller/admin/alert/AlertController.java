package com.yolt.creditscoring.controller.admin.alert;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.service.securitymodule.semaevent.InvalidSignatureDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Secured(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class AlertController {

    private final SemaEventService semaEventService;

    @PostMapping("api/admin/log/signature")
    public void logIncorrectSignature(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal,
                                      @Valid @RequestBody InvalidSignatureDTO inviteUserDTO) {
        semaEventService.logIncorrectSignatureFromExternalSystem(
                inviteUserDTO, principal.getAdminId(), principal.getClientId()
        );
    }
}
