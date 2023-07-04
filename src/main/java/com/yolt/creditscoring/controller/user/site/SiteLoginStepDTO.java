package com.yolt.creditscoring.controller.user.site;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SiteLoginStepDTO {
    
    @NonNull
    String redirectUrl;
}

