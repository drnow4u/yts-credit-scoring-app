package com.yolt.creditscoring.service.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientAdminAuditDTO {

    @NonNull
    UUID clientId;

    @NonNull
    UUID adminId;

    @NonNull
    String adminEmail;

    ClientAccessType clientAccessType;

    @Singular
    Map<String, String> details;
}
