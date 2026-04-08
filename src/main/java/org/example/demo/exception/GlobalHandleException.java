package org.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.demo.dto.ErrorResponse;
import org.example.demo.service.II18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalHandleException {
    private static final Logger log = LoggerFactory.getLogger(GlobalHandleException.class);
    private final II18nService i18nService;

    public GlobalHandleException(II18nService i18nService) {
        this.i18nService = i18nService;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handlingApiException(ApiException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        logException(exception, request, errorCode.getStatus());

        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(i18nService.getMessage(errorCode.getMessageKey()))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MessageError.class)
    public ResponseEntity<?> handlingMessageError(MessageError exception, HttpServletRequest request) {
        logException(exception, request, HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(i18nService.getMessage(exception.getMessageKey(), exception.getArgs()))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlingValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .map(this::resolveValidationMessage)
                .orElse(i18nService.getMessage("common.error.invalidRequest"));

        logException(exception, request, HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handlingMethodNotSupported(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        logException(exception, request, HttpStatus.METHOD_NOT_ALLOWED);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ErrorResponse.builder()
                .code(HttpStatus.METHOD_NOT_ALLOWED.value())
                .message(i18nService.getMessage("common.error.methodNotAllowed"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handlingAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        logException(exception, request, HttpStatus.FORBIDDEN);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message(i18nService.getMessage("common.error.accessDenied"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlingGenericException(Exception exception, HttpServletRequest request) {
        logException(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(i18nService.getMessage("common.error.internalServer"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private String resolveValidationMessage(String message) {
        return message != null && message.startsWith("validation.")
                ? i18nService.getMessage(message)
                : message;
    }

    private void logException(Exception exception, HttpServletRequest request, HttpStatus status) {
        String queryString = request.getQueryString();
        String endpoint = queryString == null || queryString.isBlank()
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + queryString;

        log.error(
                "API exception occurred. status={}, method={}, endpoint={}, message={}, timestamp={}",
                status.value(),
                request.getMethod(),
                endpoint,
                exception.getMessage(),
                LocalDateTime.now(),
                exception
        );
    }
}
