package com.yolt.creditscoring.service.client;

import com.yolt.creditscoring.configuration.validation.constraints.EmailAddress;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
public class ClientEmailDTO {

    @NonNull
    private UUID id;

    @NonNull
    private String subject;

    @NonNull
    private String template;

    private String title;

    private String subtitle;

    private String welcomeBox;

    private String buttonText;

    private String summaryBox;

    private String websiteUrl;

    @NonNull
    @EmailAddress
    private String sender;
}
