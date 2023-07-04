package com.yolt.creditscoring.configuration;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.yolt.creditscoring.configuration.security.user.UserDiagnosticContextFilter.APP_CLIENT_ID_FIELD_NAME;
import static com.yolt.creditscoring.configuration.security.user.UserDiagnosticContextFilter.APP_USER_ID_FIELD_NAME;
import static com.yolt.creditscoring.utility.tracing.TraceIdSupplier.REQUEST_TRACE_ID_MDC_FIELD_NAME;
import static nl.ing.lovebird.logging.MDCContextCreator.*;

@Configuration
public class SleuthConfiguration {

    @Bean
    BaggageField requestTraceIdBaggage() {
        return BaggageField.create(REQUEST_TRACE_ID_MDC_FIELD_NAME);
    }

    @Bean
    BaggageField clientIdBaggage() {
        return BaggageField.create(CLIENT_ID_HEADER_NAME);
    }

    @Bean
    BaggageField clientUserBaggage() {
        return BaggageField.create(CLIENT_USER_ID_HEADER_NAME);
    }

    @Bean
    BaggageField userSiteIdBaggage() {
        return BaggageField.create(USER_SITE_ID_MDC_KEY);
    }

    @Bean
    BaggageField appClientIdBaggage() {
        return BaggageField.create(APP_CLIENT_ID_FIELD_NAME);
    }

    @Bean
    BaggageField appUserIdBaggage() {
        return BaggageField.create(APP_USER_ID_FIELD_NAME);
    }


    /**
     * See https://docs.spring.io/spring-cloud-sleuth/docs/current-SNAPSHOT/reference/html/project-features.html,
     * "Note that the extra field is propagated and added to MDC starting with the next downstream trace context.
     * To immediately add the extra field to MDC in the current trace context, configure the field to flush on update:"
     */
    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(requestTraceIdBaggage())
                        .flushOnUpdate()
                        .build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(clientIdBaggage())
                        .flushOnUpdate()
                        .build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(clientUserBaggage())
                        .flushOnUpdate()
                        .build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(userSiteIdBaggage())
                        .flushOnUpdate()
                        .build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(appClientIdBaggage())
                        .flushOnUpdate()
                        .build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(appUserIdBaggage())
                        .flushOnUpdate()
                        .build())
                .build();
    }
}
