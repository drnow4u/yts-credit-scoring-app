package com.yolt.creditscoring.configuration.security;

import com.yolt.creditscoring.exception.HttpHeaderLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Limits for http headers - https://yolt.atlassian.net/browse/YTSAPP-565
 */
@Slf4j
@Component
public class HttpHeaderVerificationInterceptor extends HandlerInterceptorAdapter {

    private static final int BYTE_LIMIT_PER_HEADER = 4000;
    private static final int MAX_HEADER_COUNT = 32;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            int headersCount = 0;
            while (headerNames.hasMoreElements()) {
                String headerValue = request.getHeader(headerNames.nextElement());
                if(headerValue.getBytes(StandardCharsets.UTF_8).length > BYTE_LIMIT_PER_HEADER) {
                    throw new HttpHeaderLimitException("Header " + headerNames.nextElement() +
                            " extended available byte limit of " + BYTE_LIMIT_PER_HEADER);
                }
                headersCount++;
                if(headersCount > MAX_HEADER_COUNT) {
                    throw new HttpHeaderLimitException("Headers extended the max count limit of: " + MAX_HEADER_COUNT);
                }
            }
        }

        return super.preHandle(request, response, handler);
    }
}