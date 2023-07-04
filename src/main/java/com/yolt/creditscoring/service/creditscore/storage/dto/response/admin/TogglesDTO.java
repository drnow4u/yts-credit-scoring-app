package com.yolt.creditscoring.service.creditscore.storage.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TogglesDTO {

    private boolean categoryFeatureToggle;

    private boolean monthsFeatureToggle;

    private boolean overviewFeatureToggle;

    private boolean apiTokenFeatureToggle;

    private boolean estimateFeatureToggle;
}
