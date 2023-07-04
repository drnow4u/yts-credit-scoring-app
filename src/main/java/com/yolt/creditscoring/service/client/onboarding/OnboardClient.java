package com.yolt.creditscoring.service.client.onboarding;

import com.yolt.creditscoring.service.client.model.ClientLanguage;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OnboardClient {

    @NonNull
    private final UUID id;

    @NonNull
    private final String name;

    private final String logo;

    @NonNull
    private final ClientLanguage defaultLanguage;

    private final String additionalTextConsent;

    private final String additionalTextReport;

    @Valid
    @NonNull
    @Singular
    private final List<OnboardClientEmail> clientEmails;

    @NonNull
    private final String siteTags;

    private final String redirectUrl;

    @NonNull
    private final Boolean pDScoreFeatureToggle;

    @NonNull
    private final Boolean signatureVerificationFeatureToggle;

    @NonNull
    private final Boolean categoryFeatureToggle;

    @NonNull
    private final Boolean monthsFeatureToggle;

    @NonNull
    private final Boolean overviewFeatureToggle;

    @NonNull
    private final Boolean apiTokenFeatureToggle;
}
