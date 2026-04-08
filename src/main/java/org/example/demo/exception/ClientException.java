package org.example.demo.exception;

import org.example.demo.common.ErrorCode;

public class ClientException extends AppException {
    public ClientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
