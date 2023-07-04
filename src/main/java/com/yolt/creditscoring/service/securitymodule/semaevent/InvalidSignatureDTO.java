package com.yolt.creditscoring.service.securitymodule.semaevent;

import com.yolt.creditscoring.controller.admin.users.Based64;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Value
@Builder
public class InvalidSignatureDTO {

    @NotNull
    UUID userId;

    @NotNull
    Based64 signature;

    String message;
}
