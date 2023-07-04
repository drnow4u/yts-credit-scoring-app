package com.yolt.creditscoring.controller;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@UtilityClass
public class SecurityHelper {
    public static final String CONTENT_SECURITY_POLICY_HEADER_VALUE = "style-src 'self' 'unsafe-inline'; worker-src 'none'; child-src 'none'; script-src 'self'; frame-src 'self' blob:; connect-src 'self'; img-src 'self' data:; default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; form-action 'self'; block-all-mixed-content; upgrade-insecure-requests;";

    @NotNull
    public static ResultActions hasSecurityHeaderSetup(ResultActions resultActions) throws Exception {
        return resultActions
                .andExpect(header().string("referrer-policy", "strict-origin"))
                .andExpect(header().string("content-security-policy", CONTENT_SECURITY_POLICY_HEADER_VALUE))
                .andExpect(header().string("x-content-type-options", "nosniff"))
                .andExpect(header().string("x-frame-options", "DENY"))
                .andExpect(header().string("x-xss-protection", "1; mode=block"));
    }
}
