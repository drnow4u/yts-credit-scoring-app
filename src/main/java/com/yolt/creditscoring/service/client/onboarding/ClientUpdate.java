package com.yolt.creditscoring.service.client.onboarding;

import com.yolt.creditscoring.service.client.model.ClientLanguage;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ClientUpdate {

    @NonNull
    private final UUID id;

    private final String name;

    private final String logo;

    private final String additionalTextConsent;

    private final String additionalTextReport;

    private final String template;

    private final String siteTags;

    private final ClientLanguage defaultLanguage;

    private final String redirectUrl;

    private final Boolean pDScoreFeatureToggle;

    private final Boolean signatureVerificationFeatureToggle;

    private final Boolean categoryFeatureToggle;

    private final Boolean monthsFeatureToggle;

    private final Boolean overviewFeatureToggle;

    private final Boolean apiTokenFeatureToggle;

    private final ClientEmailUpdate clientEmailUpdate;
}
