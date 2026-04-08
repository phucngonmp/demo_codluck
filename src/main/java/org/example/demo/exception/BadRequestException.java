package org.example.demo.exception;

import org.example.demo.common.ErrorCode;

public class BadRequestException extends AppException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
