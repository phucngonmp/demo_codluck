package org.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.common.ApiResponse;
import org.example.demo.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionHandling {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        log.info("Validation failed | fields: {} | message: {} f", ex.getFieldErrors(), ex.getMessage() );
        return ApiResponse.error(ErrorCode.VALIDATIONS_FAILED, errors);
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
        return ApiResponse.error(ex.getErrorCode());
    }
}
