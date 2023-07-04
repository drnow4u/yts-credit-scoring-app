package com.yolt.creditscoring.service.client;

import com.yolt.creditscoring.service.client.model.ClientLanguage;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class ClientSettingsDTO {

    @NotNull
    private ClientLanguage defaultLanguage;

    private boolean pDScoreFeatureToggle;

    private boolean signatureVerificationFeatureToggle;

    private boolean categoryFeatureToggle;

    private boolean monthsFeatureToggle;

    private boolean overviewFeatureToggle;

    private boolean apiTokenFeatureToggle;
}
