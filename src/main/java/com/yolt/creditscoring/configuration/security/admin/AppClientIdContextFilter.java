package com.yolt.creditscoring.configuration.security.admin;


import brave.baggage.BaggageField;
import com.yolt.creditscoring.configuration.security.PrincipalHavingClientId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.JwtClaims;
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
import java.util.UUID;

/**
 * Diagnostic Filter
 * <p>
 * This configuration is adding filter to inject user diagnostic data in MDC to have it visible in Kibana.
 * New key has to be whitelisted in application.yml
 * <p>
 * Injected fields:
 * app-client-id - client ID in this application
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AppClientIdContextFilter extends OncePerRequestFilter {

    private final BaggageField appClientIdBaggage;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .flatMap(principal -> {
                    if (principal instanceof JwtClaims jwtClaims) {
                        return Optional.ofNullable((String) jwtClaims.getClaimValue(AdminClaims.CLIENT_ID))
                                .map(UUID::fromString);
                    }
                    if (principal instanceof PrincipalHavingClientId principalHavingClientId) {
                        return Optional.of(principalHavingClientId.getClientId());
                    }
                    return Optional.empty();
                })
                .ifPresent(clientId -> appClientIdBaggage.updateValue(clientId.toString()));

        filterChain.doFilter(request, response);

    }
}
