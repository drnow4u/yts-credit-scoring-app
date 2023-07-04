package com.yolt.creditscoring.configuration.security;


import brave.baggage.BaggageField;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter that sets the yolt client id on the baggage (and MDC)
 */
@RequiredArgsConstructor
@Component
public class ClientIdFilter extends OncePerRequestFilter {

    @NonNull
    @Value("${yolt.yolt-api.client-id}")
    private final UUID yoltClientId;

    @NonNull
    private final BaggageField clientIdBaggage;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        clientIdBaggage.updateValue(yoltClientId.toString());

        filterChain.doFilter(request, response);
    }
}
