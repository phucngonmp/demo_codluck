package org.example.demo.exception;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNAUTHENTICATION (401, "auth.error.unauthenticated", HttpStatus.UNAUTHORIZED);
    Integer code;
    String messageKey;
    HttpStatus status;
}
