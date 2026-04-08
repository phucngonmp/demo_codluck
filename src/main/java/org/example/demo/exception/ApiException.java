package org.example.demo.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{
    ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
    }
}
