package com.yolt.creditscoring.service.securitymodule.signature;

import com.yolt.creditscoring.controller.admin.users.Based64;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ReportSignature {
    @NonNull
    Based64 signature;

    @NonNull
    UUID keyId;

    @NonNull
    List<String> jsonPaths;
}
