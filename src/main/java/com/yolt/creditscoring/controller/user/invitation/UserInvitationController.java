package com.yolt.creditscoring.controller.user.invitation;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.usecase.UserInvitationUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class UserInvitationController {

    public static final String USER_INVITATION_ENDPOINT = "/api/user/token/{hash}";
    public static final String USER_CONSENT_ENDPOINT = "/api/user/consent";
    private final UserInvitationUseCase userInvitationService;

    /**
     * Note that this endpoint is an 'open' endpoint. Doesn't require any authentication.
     */
    @GetMapping(USER_INVITATION_ENDPOINT)
    public ConsentViewDTO validateUser(@PathVariable String hash) {
        return userInvitationService.validateUser(hash);
    }

    @Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
    @PostMapping(USER_CONSENT_ENDPOINT)
    public void saveUserConsent(@AuthenticationPrincipal CreditScoreUserPrincipal principal,
                                @RequestBody ConsentUserDTO consentUserDTO,
                                @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                HttpServletRequest request) {
        userInvitationService.saveLoggedUserConsent(principal.getUserId(), consentUserDTO.isConsent(), userAgent, request.getRemoteAddr());
    }
}
