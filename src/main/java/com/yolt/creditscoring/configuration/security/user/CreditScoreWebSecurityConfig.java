package com.yolt.creditscoring.configuration.security.user;

import com.yolt.creditscoring.configuration.security.WebSecurityConfig;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.UserStorageService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for credit score user.
 * The entry point for this configuration in "/api/**"
 * <p>
 * JWT is created when user clicks the invitation link in the email message, which "logins" user in the application
 * <p>
 * Permits only user with CREDIT_SCORE_USER role.
 * "/api/token/*" endpoint is open for validating the invitation link and is responsible for JWT creation
 */
@Configuration
public class CreditScoreWebSecurityConfig {

    private final JwtCreationService jwtCreationService;
    private final UserStorageService userStorageService;
    private final UserDiagnosticContextFilter userDiagnosticContextFilter;

    public CreditScoreWebSecurityConfig(JwtCreationService jwtCreationService,
                                        UserStorageService userStorageService,
                                        UserDiagnosticContextFilter userDiagnosticContextFilter) {
        this.jwtCreationService = jwtCreationService;
        this.userStorageService = userStorageService;
        this.userDiagnosticContextFilter = userDiagnosticContextFilter;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .antMatcher("/api/user/**")
                .addFilterAfter(new UserJwtAuthorizationFilter(jwtCreationService, userStorageService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new UserFlowVerificationFilter(userStorageService), UserJwtAuthorizationFilter.class)
                .addFilterAfter(userDiagnosticContextFilter, UserFlowVerificationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/user/token/*").permitAll()
                .anyRequest().authenticated();
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        WebSecurityConfig.setupSecurityHeaders(http);

        return http.build();
    }

    /**
     * Prevent the {@link UserDiagnosticContextFilter} to register on the original application filter chain.
     * We are adding it manually to the spring security filter chain.
     * I don't like it, but spring is making it hard to use it's own dependency injection by autoregistering filters
     * on the application filter chain. Also see
     * https://github.com/spring-projects/spring-boot/issues/2173 ,
     * https://stackoverflow.com/questions/28421966/prevent-spring-boot-from-registering-a-servlet-filter
     *
     * @param userDiagnosticContextFilter The filter
     * @return The registration bean
     */
    @Bean
    public FilterRegistrationBean<UserDiagnosticContextFilter> userDiagnosticContextFilterFilterRegistrationBean(
            UserDiagnosticContextFilter userDiagnosticContextFilter) {
        FilterRegistrationBean<UserDiagnosticContextFilter> registration = new FilterRegistrationBean<>(userDiagnosticContextFilter);
        registration.setEnabled(false);
        return registration;
    }
}
