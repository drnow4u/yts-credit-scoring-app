package com.yolt.creditscoring.configuration.security.admin;

import lombok.NonNull;

public record CfaAdminPrincipal(@NonNull String email, @NonNull String idpId) {
}
