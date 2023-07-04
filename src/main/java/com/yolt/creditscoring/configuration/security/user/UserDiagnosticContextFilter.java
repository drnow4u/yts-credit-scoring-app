package com.yolt.creditscoring.configuration.security.user;


import brave.baggage.BaggageField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Diagnostic Filter
 * <p>
 * This configuration is adding filter to inject user diagnostic data in MDC to have it visible in Kibana.
 * New key has to be whitelisted in application.yml
 * <p>
 * Injected fields:
 * client-id - client ID in YTS
 * client_user_id - user ID in YTS API, because of security in YTS is different then user-id
 * user_site_id - same as YTS
 * app-client-id - client ID in this application
 * app-user-id - user ID in this application
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UserDiagnosticContextFilter extends OncePerRequestFilter {

    public static final String APP_CLIENT_ID_FIELD_NAME = "app-client-id";
    public static final String APP_USER_ID_FIELD_NAME = "app-user-id";

    private final BaggageField clientUserBaggage;
    private final BaggageField userSiteIdBaggage;
    private final BaggageField appClientIdBaggage;
    private final BaggageField appUserIdBaggage;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(CreditScoreUserPrincipal.class::cast)
                .ifPresent(user -> {
                    if (user.getYoltUserId() != null) {
                        // In pod client-gateway (client-user) is mapping external client_user_id to user_id. Mapping is visible in Assistant Portal
                        clientUserBaggage.updateValue(user.getYoltUserId().toString());
                    }
                    if (user.getYoltUserSiteId() != null) {
                        userSiteIdBaggage.updateValue(user.getYoltUserSiteId().toString());
                    }
                    appClientIdBaggage.updateValue(user.getClientId().toString());
                    appUserIdBaggage.updateValue(user.getUserId().toString());
                });

        filterChain.doFilter(request, response);
    }

}
