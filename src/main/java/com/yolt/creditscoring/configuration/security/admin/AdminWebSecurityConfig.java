package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.WebSecurityConfig;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.RequiredArgsConstructor;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;

/**
 * Security configuration for client admin user.
 * The entry point for this configuration in "/api/admin/**"
 * <p>
 * Uses spring oauth to authenticate user via Github, Microsoft or Google provider
 * <p>
 * Permits only user with CLIENT_ADMIN role.
 * "/api/admin/oauth2/**" to open Github, Microsoft, Google login in.
 */
@Configuration
@RequiredArgsConstructor
public class AdminWebSecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final ProxySelectorFactory proxySelectorFactory;
    private final JwtCreationService jwtCreationService;
    private final ClientAdminRepository clientAdminRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final CustomLogoutHandler logoutHandler;
    private final AppClientIdContextFilter appClientIdContextFilter;
    private final TestCfaAdminProperties testCfaAdminProperties;

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .requestMatchers()
                .antMatchers("/api/admin/**", "/api/management/**")
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/api/admin/oauth2/**").permitAll()
                .anyRequest().authenticated();

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .authorizationEndpoint()
                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                .baseUri("/api/admin/oauth2")
                .and()
                .redirectionEndpoint()
                .baseUri("/api/admin/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(oauth2UserService())
                .oidcUserService(oidcUserService())
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .and()
                .logout()
                .logoutUrl("/api/admin/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK));

        http.addFilterAfter(new ClientAdminJwtAuthorizationFilter(jwtCreationService), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(appClientIdContextFilter, ClientAdminJwtAuthorizationFilter.class);

        WebSecurityConfig.setupSecurityHeaders(http);

        return http.build();
    }

    /**
     * Additional configuration for OAuth2UserService to allow proxy calls
     *
     * @return CustomOAuth2UserService with configured proxy
     */
    @Bean
    public CustomOAuth2UserService oauth2UserService() {
        CustomOAuth2UserService userService = new CustomOAuth2UserService(clientAdminRepository, jwtCreationService, testCfaAdminProperties);

        RestTemplate restTemplate = getRestTemplateBuilderWithProxy().build();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        userService.setRestOperations(restTemplate);
        return userService;
    }

    @Bean
    public CustomOidcUserService oidcUserService() {
        return new CustomOidcUserService(oauth2UserService());
    }

    /**
     * Additional configuration for OAuth2AccessTokenResponseClient to allow proxy calls
     *
     * @return DefaultAuthorizationCodeTokenResponseClient with configured proxy
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

        RestTemplate restTemplate = getRestTemplateBuilderWithProxy()
                .messageConverters(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()))
                .build();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;
    }

    private RestTemplateBuilder getRestTemplateBuilderWithProxy() {
        return new RestTemplateBuilder()
                .requestFactory(() -> new BufferingClientHttpRequestFactory(
                        new OkHttp3ClientHttpRequestFactory(new OkHttpClient.Builder()
                                .proxySelector(proxySelectorFactory.create())
                                .certificatePinner(new CertificatePinner.Builder().build())
                                .build())
                ))
                .setReadTimeout(Duration.ofSeconds(10));
    }

    @Bean
    public JwtDecoderFactory<ClientRegistration> customJwtDecoderFactory() {
        return this::jwtDecoder;
    }

    private JwtDecoder jwtDecoder(ClientRegistration clientRegistration) {
        RestTemplate restTemplate = getRestTemplateBuilderWithProxy().build();
        return NimbusJwtDecoder
                .withJwkSetUri(clientRegistration.getProviderDetails().getJwkSetUri())
                .restOperations(restTemplate)
                .build();
    }

    /**
     * Prevent the {@link AppClientIdContextFilter} to register on the original application filter chain.
     * We are adding it manually to the spring security filter chain.
     * I don't like it, but spring is making it hard to use it's own dependency injection by autoregistering filters
     * on the application filter chain. Also see
     * https://github.com/spring-projects/spring-boot/issues/2173 ,
     * https://stackoverflow.com/questions/28421966/prevent-spring-boot-from-registering-a-servlet-filter
     *
     * @param appClientIdContextFilter The filter
     * @return The registration bean
     */
    @Bean
    public FilterRegistrationBean<AppClientIdContextFilter> appClientIdContextFilterFilterRegistrationBean(
            AppClientIdContextFilter appClientIdContextFilter) {
        FilterRegistrationBean<AppClientIdContextFilter> registration = new FilterRegistrationBean<>(appClientIdContextFilter);
        registration.setEnabled(false);
        return registration;
    }
}
