package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.creditscoring.configuration.security.admin.CustomOAuth2UserService.CFA_ADMIN_SECURITY_GROUP;
import static com.yolt.creditscoring.configuration.security.admin.CustomOAuth2UserService.YOLT_MICROSOFT_TENANT;

/**
 * Data that comes directly from the {@link AuthProvider}, in a normalized object containing the idpId and email.
 */
record OAuth2UserInfo(
        @NonNull String idpId,
        @Nullable String email,
        @NonNull AuthProvider oAuthProvider,
        @NonNull Map<String, Object> originalAttributes,
        @Nullable MicrosoftInfo microsoftInfo
) {
    private static final String NAME_ATTRIBUTE_KEY = "idpId";
    private static final String EMAIL_KEY = "email";

    boolean hasCfaAdminSecurityRoleInYoltTenant() {
        if (microsoftInfo == null) {
            return false;
        }
        List<String> groups = microsoftInfo.groups();
        return YOLT_MICROSOFT_TENANT.equals(microsoftInfo.tenant())
                && groups != null && groups.contains(CFA_ADMIN_SECURITY_GROUP.toString());
    }

    Map<String, Object> getAllUserAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(NAME_ATTRIBUTE_KEY, idpId);
        if (email != null) {
            attributes.put(EMAIL_KEY, email);
        }
        attributes.putAll(originalAttributes);
        return attributes;
    }
}

record MicrosoftInfo (
        @NonNull UUID tenant,
        @Nullable List<String> groups
){
}
