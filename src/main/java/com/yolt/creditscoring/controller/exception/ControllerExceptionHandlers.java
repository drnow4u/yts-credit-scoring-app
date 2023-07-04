package com.yolt.creditscoring.controller.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.controller.admin.estimate.FeatureToggleDisableException;
import com.yolt.creditscoring.exception.*;
import com.yolt.creditscoring.service.client.ClientFeatureDisabledException;
import com.yolt.creditscoring.service.clienttoken.TooManyTokensException;
import com.yolt.creditscoring.service.yoltapi.exception.SiteAuthenticationException;
import com.yolt.creditscoring.service.yoltapi.exception.SiteCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static nl.ing.lovebird.logging.MDCContextCreator.ENDPOINT_MDC_KEY;

/**
 * Error handling logic
 * For form validation object {@link FormValidationErrorResponse} should be returned.
 * <p>
 * All 400 (Bad request) errors should return object {@link ErrorResponseDTO} with given {@link ErrorType} - those error
 * types are change to error messages in the frontend application - check ErrorComponent.tsx for reference.
 * <p>
 * General exceptions should be catch in handleGeneralException method and map the status code to 500.
 * Those exceptions are logged and the frontend application receives only the error code that is displayed to the user
 * from object {@link ErrorResponseDTO}.
 * <p>
 * Method {@link #serializeErrorResponse} should by used in places where annotation {@link ControllerAdvice} is not
 * working e.g. Spring Security
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandlers {

    public static final String ERROR_CODE_MDC_KEY = "app-error-code";

    @ExceptionHandler(ReportNotReadyException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void handle(ReportNotReadyException ex) {
        // When the report is not generated yet we want to only return 202 status code
    }

    @ExceptionHandler(InviteStillPendingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handle(InviteStillPendingException e) {
        log.warn(e.getMessage());
        return new ErrorResponseDTO(ErrorType.INVITE_STILL_PENDING);
    }

    @ExceptionHandler(ClientEmailConfigurationNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handle(ClientEmailConfigurationNotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResponseDTO(ErrorType.CLIENT_EMAIL_NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponseDTO handle(UserNotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResponseDTO(ErrorType.USER_NOT_FOUND);
    }

    @ExceptionHandler(InvitationExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    protected ErrorResponseDTO handleInvitationExpiredException(InvitationExpiredException e) {
        log.warn(e.getMessage());
        return new ErrorResponseDTO(ErrorType.INVITATION_EXPIRED);
    }

    @ExceptionHandler(UserSiteAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handleUserSiteAlreadyExistException() {
        return new ErrorResponseDTO(ErrorType.BANK_CONNECTION_EXIST);
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handleInvalidStatusException() {
        return new ErrorResponseDTO(ErrorType.FLOW_ENDED);
    }

    @ExceptionHandler(SiteCreationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handleSiteCreationException(SiteCreationException e) {
        log.error(e.getMessage());
        return new ErrorResponseDTO(ErrorType.BANK_CONNECTION_FAILURE);
    }

    @ExceptionHandler(SiteAuthenticationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handleSiteAuthenticationException(SiteAuthenticationException e) {
        log.info(e.getMessage());
        return new ErrorResponseDTO(ErrorType.BANK_CONSENT_REFUSED);
    }

    @ExceptionHandler(ClientTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponseDTO handle(ClientTokenNotFoundException e) {
        log.info(e.getMessage());
        return new ErrorResponseDTO(ErrorType.TOKEN_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponseDTO handleGeneralException(Exception ex, HttpServletRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        MDC.put(ENDPOINT_MDC_KEY, request.getRequestURI());
        MDC.put(ERROR_CODE_MDC_KEY, errorResponseDTO.getErrorCode().toString());
        log.error(ex.getMessage(), ex);
        MDC.remove(ENDPOINT_MDC_KEY);
        MDC.remove(ERROR_CODE_MDC_KEY);
        return errorResponseDTO;
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handle(AccessDeniedException e) {
        // We don't want to 'catch' this in a 'handleGeneralException', because we want it to bubble up to a
        // AccessDeniedHandler that is configured.
        throw e;
    }

    @ExceptionHandler(ClientFeatureDisabledException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handle(ClientFeatureDisabledException e) {
        log.info(e.getMessage());
        return new ErrorResponseDTO(ErrorType.FEATURE_DISABLED);
    }

    @ExceptionHandler(TooManyTokensException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseDTO handle(TooManyTokensException e) {
        log.info(e.getMessage());
        return new ErrorResponseDTO(ErrorType.TOO_MANY_TOKENS);
    }

    @ExceptionHandler(FeatureToggleDisableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<String> handle(FeatureToggleDisableException exception) {
        log.error(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Not allowed");
    }

    @ExceptionHandler(CreditScoreReportNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handle(CreditScoreReportNotFoundException exception) {
        log.error(exception.getMessage());
        return new ErrorResponseDTO(ErrorType.REPORT_NOT_FOUND);
    }

    public static void serializeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorResponseDTO errorResponse,
            String errorMessage
    ) throws IOException {
        try (OutputStream out = response.getOutputStream()) {
            MDC.put(ENDPOINT_MDC_KEY, request.getRequestURI());
            MDC.put(ERROR_CODE_MDC_KEY, errorResponse.getErrorCode().toString());
            log.error(errorMessage);
            new ObjectMapper().writeValue(out, errorResponse);
            MDC.remove(ENDPOINT_MDC_KEY);
            MDC.remove(ERROR_CODE_MDC_KEY);
        }
    }

    public static void serializeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorResponseDTO errorResponse,
            Exception exception
    ) throws IOException {
        try (OutputStream out = response.getOutputStream()) {
            MDC.put(ENDPOINT_MDC_KEY, request.getRequestURI());
            MDC.put(ERROR_CODE_MDC_KEY, errorResponse.getErrorCode().toString());
            log.error(exception.getMessage(), exception);
            new ObjectMapper().writeValue(out, errorResponse);
            MDC.remove(ENDPOINT_MDC_KEY);
            MDC.remove(ERROR_CODE_MDC_KEY);
        }
    }
}
