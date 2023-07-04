package com.yolt.creditscoring.service.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.UUID;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAuditDTO {

    @Nullable
    UUID clientId;

    @Nullable
    UUID adminId;

    @NonNull
    String idpId;

    @NonNull
    String adminEmail;

    ClientAccessType clientAccessType;

    @Singular
    Map<String, String> details;
}
