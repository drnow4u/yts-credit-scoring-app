package com.yolt.creditscoring.controller.user.site;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.service.yoltapi.dto.LoginResponse;
import com.yolt.creditscoring.usecase.SiteConnectionUseCase;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AllArgsConstructor
@RestController
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
public class SiteController {

    public static final String SITES_ENDPOINT = "/api/user/sites";
    public static final String SITES_CONNECT_ENDPOINT = "/api/user/sites/connect";
    public static final String USER_SITE_ENDPOINT = "/api/user/sites/user-site";
    private final SiteConnectionUseCase siteConnectionUseCase;

    @GetMapping(value = SITES_ENDPOINT)
    public List<SiteViewDTO> sites(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return siteConnectionUseCase.getSitesForClient(principal.getClientId());
    }

    @PostMapping(value = SITES_CONNECT_ENDPOINT)
    public SiteLoginStepDTO requestUserConsent(@AuthenticationPrincipal CreditScoreUserPrincipal principal,
                                               @RequestBody SiteConnectRequestDTO siteConnect,
                                               HttpServletRequest request) {

        return siteConnectionUseCase.requestUserConsent(
                principal.getUserId(),
                principal.getYoltUserId(),
                principal.getYoltUserSiteId(),
                siteConnect.getSiteId(),
                request.getRemoteAddr()
        );
    }

    @PostMapping(value = USER_SITE_ENDPOINT)
    public LoginResponse createUserSite(@AuthenticationPrincipal CreditScoreUserPrincipal principal,
                                        @RequestBody UserSiteDTO siteConnect,
                                        HttpServletRequest request) {

        return siteConnectionUseCase.createUserSite(
                principal.getUserId(),
                principal.getYoltUserId(),
                siteConnect.getUrl(),
                request.getRemoteAddr(),
                principal.getClientId()
        );
    }

}
