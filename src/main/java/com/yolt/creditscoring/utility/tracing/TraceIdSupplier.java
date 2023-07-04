package com.yolt.creditscoring.utility.tracing;

import com.yolt.creditscoring.configuration.security.RequestTraceIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Supplier is generating unique trace id as UUID and store it in MDC to correlate traffic with YTS API.
 * In YTS API it is handled by <a href="https://git.yolt.io/backend/client-gateway/-/blob/master/client-proxy/src/main/java/nl/ing/lovebird/clientproxy/filter/filters/pre/RequestTraceIdHeaderFilter.java">RequestTraceIdHeaderFilter.java</a>
 * This is not official YTS API at https://developer.yolt.com/
 * <p>
 * MDC is cleaned by {@link RequestTraceIdFilter}
 */
@Slf4j
public class TraceIdSupplier implements Supplier<String> {

    public static final String REQUEST_TRACE_ID_MDC_FIELD_NAME = "request_trace_id";

    @Override
    public String get() {
        if (MDC.get(REQUEST_TRACE_ID_MDC_FIELD_NAME) != null) {
            return MDC.get(REQUEST_TRACE_ID_MDC_FIELD_NAME); //NOSHERIFF Used for request trace id
        }
        String traceId = UUID.randomUUID().toString();
        MDC.put(REQUEST_TRACE_ID_MDC_FIELD_NAME, traceId);
        // We always except a request trace id on the MDC due to the RequestTraceIdFilter.
        log.warn("No request trace id found on MDC. Generated a new one.");
        return traceId;
    }
}
