package com.yolt.creditscoring.service.client.onboarding;

import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.Email;

@Value
@Builder
public class ClientAdminOnboarding {

    @NonNull
    private final String idpId;

    @NonNull
    private final AuthProvider authProvider;

    @Email
    @NonNull
    private final String email;
}