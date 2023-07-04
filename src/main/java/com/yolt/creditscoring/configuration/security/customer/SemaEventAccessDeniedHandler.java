package com.yolt.creditscoring.configuration.security.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.yolt.creditscoring.controller.exception.ErrorType.UNKNOWN;

@RequiredArgsConstructor
@Component
@Slf4j
public class SemaEventAccessDeniedHandler implements AccessDeniedHandler {

    private final SemaEventService semaEventService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("Access denied", accessDeniedException);
        String requestURI = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof ClientTokenPrincipal clientTokenPrincipal) {
            semaEventService.logClientTokenAccessToUnauthorizedEndpoint(clientTokenPrincipal.getClientId(), requestURI, authentication.getAuthorities().stream().toList());
        }
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter out = response.getWriter();
        objectMapper.writeValue(out, new ErrorResponseDTO(UNKNOWN));
    }
}
