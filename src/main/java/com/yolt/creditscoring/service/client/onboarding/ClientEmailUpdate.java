package com.yolt.creditscoring.service.client.onboarding;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ClientEmailUpdate {
    @NonNull
    private final String title;
    @NonNull
    private final String subtitle;
    @NonNull
    private final String welcomeBox;
    @NonNull
    private final String buttonText;
    @NonNull
    private final String summaryBox;
    @NonNull
    private final String websiteUrl;
}
