package com.yolt.creditscoring.configuration.security;


import brave.baggage.BaggageField;
import com.yolt.creditscoring.utility.tracing.TraceIdSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter generate new Request Trace Id in MDC.
 * New key has to be whitelisted in application.yml
 * <p>
 * Moreover, it prevents pollution of thread local with old trace id stored in MDC.
 * Trace id is used in {@link com.yolt.creditscoring.service.yoltapi.http.YoltHttpClient}
 * with {@link TraceIdSupplier}.
 */
@Component
@RequiredArgsConstructor
public class RequestTraceIdFilter extends OncePerRequestFilter {

    private final BaggageField requestTraceIdBaggage;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        requestTraceIdBaggage.updateValue(UUID.randomUUID().toString());
        filterChain.doFilter(request, response);

    }
}
