package org.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.common.ApiResponse;
import org.example.demo.common.ErrorCode;
import org.example.demo.i18n.Translator;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {
    private final Translator translator;

    public ExceptionHandling(Translator translator) {
        this.translator = translator;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            if (isTypeMismatch(error)) {
                errors.put(error.getField(), buildTypeMismatchMessage(error));
            } else {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        });

        log.info("MethodArgumentNotValidException at API: {} | Method: {}| fields: {} | message: {}",
                request.getRequestURI(),
                request.getMethod(),
                errors.keySet(),
                errors.values());

        return ApiResponse.error(
                ErrorCode.VALIDATIONS_FAILED,
                translator.get(ErrorCode.VALIDATIONS_FAILED.getMessageKey()),
                errors
        );
    }

    private boolean isTypeMismatch(FieldError error) {
        return error.getCodes() != null &&
                java.util.Arrays.stream(error.getCodes())
                        .anyMatch(code -> code != null && code.contains("typeMismatch"));
    }

    private String buildTypeMismatchMessage(FieldError error) {
        Object rejectedValue = error.getRejectedValue();
        return "Failed to convert value '" + rejectedValue + "' for field '" + error.getField() + "'";
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClientException.class)
    public ApiResponse<?> handleClientException(ClientException ex, HttpServletRequest request) {
        log.info(
                "Client exception at API: {} | Method: {} | ErrorCode: {} | Message: {}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getErrorCode(),
                ex.getMessage()
        );

        return ApiResponse.error(ex.getErrorCode(), translator.get(ex.getErrorCode().getMessageKey()));
    }


}
