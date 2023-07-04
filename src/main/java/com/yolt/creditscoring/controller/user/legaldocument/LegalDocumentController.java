package com.yolt.creditscoring.controller.user.legaldocument;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.user.CreditScoreUserPrincipal;
import com.yolt.creditscoring.service.legaldocument.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)
@RestController
@RequiredArgsConstructor
public class LegalDocumentController {

    public static final String TERMS_CONDITIONS_ENDPOINT = "/api/user/legal-document/terms-conditions";
    public static final String PRIVACY_POLICY_ENDPOINT = "/api/user/legal-document/privacy-policy";
    private final LegalDocumentService termsAndConditionsService;

    @GetMapping(TERMS_CONDITIONS_ENDPOINT)
    public LegalDocumentViewDTO getTermsAndConditions(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return LegalDocumentViewDTO.builder()
                .html(termsAndConditionsService.getCurrentTermsAndConditions().getContent())
                .build();
    }

    @GetMapping(PRIVACY_POLICY_ENDPOINT)
    public LegalDocumentViewDTO getPrivacyPolicy(@AuthenticationPrincipal CreditScoreUserPrincipal principal) {
        return LegalDocumentViewDTO.builder()
                .html(termsAndConditionsService.getCurrentPrivacyPolicy().getContent())
                .build();
    }
}
