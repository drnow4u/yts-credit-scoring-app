package com.yolt.creditscoring.configuration.security;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import com.yolt.creditscoring.configuration.security.admin.AdminWebSecurityConfig;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * The web security for the application is divided base client admin, client token and credit score user.
 * It is done due to separate authorizations for those users.
 *
 * {@link AdminWebSecurityConfig} contains the configuration
 * for the client admin user. It is marked as the first ordered configuration with entry point "/api/admin"
 *
 * {@link com.yolt.creditscoring.configuration.security.customer.ClientTokenWebSecurityConfig} contains the configuration
 * for the client token API. It is marked as the second ordered configuration with entry point "/api/customer"
 *
 * {@link com.yolt.creditscoring.configuration.security.user.CreditScoreWebSecurityConfig} contains the configuration
 * for credit score user. It is marked as the third ordered configuration with entry point "/api"
 *
 * {@link FrontendWebSecurityConfig } contains the configuration for frontend resources. It should be executed as last
 * in the security order and be available from root context "/"
 */
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
@EnableWebSecurity
public class WebSecurityConfig {

    private static final int LEAP_YEAR_IN_SECONDS = 366 * 24 * 60 * 60;
    private static final String CSP_POLICY = String.join(
            " ",
            "style-src 'self' 'unsafe-inline';",
            "worker-src 'none';",
            "child-src 'none';",
            "script-src 'self';",
            "frame-src 'self' blob:;",            
            "connect-src 'self';",
            "img-src 'self' data:;",
            "default-src 'self';",
            "base-uri 'self';",
            "object-src 'none';",
            "frame-ancestors 'none';",
            "form-action 'self';",
            "block-all-mixed-content;",
            "upgrade-insecure-requests;"
    );

    public static void setupSecurityHeaders(HttpSecurity http) throws Exception {
        http.headers()
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(LEAP_YEAR_IN_SECONDS)
                .and()
                .xssProtection()
                .xssProtectionEnabled(true)
                .block(true)
                .and()
                .contentSecurityPolicy(CSP_POLICY)
                .and()
                .contentTypeOptions()
                .and()
                .frameOptions()
                .deny()
                .cacheControl()
                .and()
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN);
    }
}
