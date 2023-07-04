package com.yolt.creditscoring.configuration.security.customer;

import com.yolt.creditscoring.configuration.security.WebSecurityConfig;
import com.yolt.creditscoring.configuration.security.admin.AppClientIdContextFilter;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for client token API.
 * The entry point for this configuration in "/api/customer/**"
 * <p>
 * Permits only users with CLIENT_TOKEN role.
 */
@Configuration
@RequiredArgsConstructor
public class ClientTokenWebSecurityConfig {

    private final JwtCreationService jwtCreationService;
    private final ClientTokenRepository clientTokenRepository;
    private final SemaEventAccessDeniedHandler semaEventAccessDeniedHandler;
    private final AppClientIdContextFilter appClientIdContextFilter;

    @Bean
    @Order(2)
    public SecurityFilterChain clientTokenFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .antMatcher("/api/customer/**")
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .accessDeniedHandler(semaEventAccessDeniedHandler)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterAfter(new ClientTokenJwtAuthorizationFilter(jwtCreationService, clientTokenRepository), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(appClientIdContextFilter, ClientTokenJwtAuthorizationFilter.class);

        WebSecurityConfig.setupSecurityHeaders(http);

        return http.build();
    }
}
