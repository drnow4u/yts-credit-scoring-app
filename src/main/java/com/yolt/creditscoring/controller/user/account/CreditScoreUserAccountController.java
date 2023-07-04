package com.yolt.creditscoring.controller.user.account;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.usecase.SiteConnectionUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
public class CreditScoreUserAccountController {

    public static final String USER_ACCOUNTS_ENDPOINT = "/api/user/accounts";
    public static final String USER_ACCOUNTS_SELECT_ENDPOINT = "/api/user/accounts/select";
    private final SiteConnectionUseCase siteConnectionUseCase;

    @GetMapping(USER_ACCOUNTS_ENDPOINT)
    public ResponseEntity<List<Account>> accounts(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return siteConnectionUseCase.wasUserSiteDataFetched(principal.getUserId())
                ? ResponseEntity.ok(siteConnectionUseCase.getAccounts(principal.getYoltUserId()))
                : ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping(USER_ACCOUNTS_SELECT_ENDPOINT)
    public ResponseEntity<Void> selectAccount(@AuthenticationPrincipal CreditScoreUserPrincipal principal,
                                              @RequestBody SelectAccountDTO selectAccountDTO) {

        siteConnectionUseCase.updateAccountForUser(principal.getUserId(), selectAccountDTO.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
