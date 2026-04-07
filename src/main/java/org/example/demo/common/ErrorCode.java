package org.example.demo.common;

public enum ErrorCode {

    TOKEN_NULL(1, "Token is null"),
    TOKEN_INVALID(2, "token is invalid"),
    BAD_CREDENTIALS(3, "sai tên đăng nhập hoặc mật khẩu"),
    EMAIL_EXISTED(4, "email đã tồn tại"),
    USERNAME_EXISTED(5, "username đã tồn tại");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

