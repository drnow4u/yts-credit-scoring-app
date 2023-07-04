package com.yolt.creditscoring.configuration.security.customer;

import com.yolt.creditscoring.configuration.security.PrincipalHavingClientId;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

/**
 * Please note that this is an "anonymous" token for a given client. A client-backend will use this token to
 * connect to the CFA - API.
 * It's a little odd that it contains an email. This is the email address of the client-admin that created the
 * "anonymous" token. Also see YTRN-1290
 *
 */
@Value
@Builder
public class ClientTokenPrincipal implements PrincipalHavingClientId {

    ClientAccessType clientAccessType = ClientAccessType.TOKEN;

    @NonNull
    UUID tokenId;

    @NonNull
    UUID clientId;

    @NonNull
    String email;
}
