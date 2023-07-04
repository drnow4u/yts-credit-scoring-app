package com.yolt.creditscoring.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Contains security configuration for the frontend resources.
 * Each new frontend endpoint should be explicitly add to the matcher.
 * <p>
 * Since it is the last security order and applies to root context
 * it is automatically rejecting other request ".anyRequest().denyAll();"
 */
@Configuration
public class FrontendWebSecurityConfig {

    @Bean
    @Order(4)
    public SecurityFilterChain frontendFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/actuator/**",
                        "/",
                        "/favicon.ico",
                        "/*.svg",
                        "/*.png",
                        "/*.jpg",
                        "/manifest.json",
                        "/asset-manifest.json",
                        "/static/css/*.css",
                        "/static/js/*.js",
                        "/static/media/**",
                        "/favicons/**",
                        "/mail/**",
                        "/admin",
                        "/admin/login",
                        "/admin/legal",
                        "/admin/privacy-statement",
                        "/admin/dashboard",
                        "/admin/oauth2/callback/github",
                        "/admin/oauth2/callback/google",
                        "/admin/oauth2/callback/microsoft",
                        "/admin/report/**",
                        "/admin/statistics",
                        "/admin/settings",
                        "/consent/**",
                        "/consent-refused/**",
                        "/select-bank/**",
                        "/site-connect-callback/**",
                        "/select-account/**",
                        "/credit-report/**",
                        /*
                        //YTRN-1261 - moved client logo to database
                         */
                        "/clients/*/logo"
                ).permitAll()
                .anyRequest().denyAll();

        WebSecurityConfig.setupSecurityHeaders(http);

        return http.build();
    }
}
