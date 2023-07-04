package com.yolt.creditscoring.configuration;

import com.yolt.creditscoring.configuration.security.HttpHeaderVerificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final HttpHeaderVerificationInterceptor productServiceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(productServiceInterceptor);
    }

}