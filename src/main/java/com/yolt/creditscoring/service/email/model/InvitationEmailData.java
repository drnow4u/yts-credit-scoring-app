package com.yolt.creditscoring.service.email.model;

import com.yolt.creditscoring.configuration.validation.constraints.EmailAddress;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.Valid;

@Builder
@Value
public class InvitationEmailData {
    @Valid
    @NonNull
    ClientEmailDTO clientEmail;
    @NonNull
    @EmailAddress
    String recipientEmail;
    @NonNull
    String userName;
    @NonNull
    String clientLogoUrl;
    @NonNull
    String redirectUrl;
}
