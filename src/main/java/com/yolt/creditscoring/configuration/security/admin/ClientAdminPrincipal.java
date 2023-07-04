package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.PrincipalHavingClientId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ClientAdminPrincipal implements PrincipalHavingClientId {

    ClientAccessType clientAccessType = ClientAccessType.ADMIN;

    @NonNull
    UUID adminId;

    @NonNull
    UUID clientId;

    @NonNull
    String email;

    @NonNull
    String idpId;
}
