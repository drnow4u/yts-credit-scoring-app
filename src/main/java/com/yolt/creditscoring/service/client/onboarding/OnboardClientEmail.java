package com.yolt.creditscoring.service.client.onboarding;

import com.yolt.creditscoring.configuration.validation.constraints.EmailAddress;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OnboardClientEmail {

    @NonNull
    private final String template;

    @NonNull
    private final String subject;

    @NonNull
    @EmailAddress
    private final String sender;
}