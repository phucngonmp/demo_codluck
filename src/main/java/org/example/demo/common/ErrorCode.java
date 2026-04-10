package org.example.demo.common;

public enum ErrorCode {

    TOKEN_NULL(1, "security.token.null"),
    TOKEN_INVALID(2, "security.token.invalid"),
    BAD_CREDENTIALS(401, "auth.login.badCredentials"),
    EMAIL_EXISTED(4, "auth.register.emailExisted"),
    USERNAME_EXISTED(5, "auth.register.usernameExisted"),
    USERNAME_NOT_FOUND(6, "auth.user.usernameNotFound"),
    PASSWORD_MISMATCH(7, "auth.register.passwordMismatch"),
    VALIDATIONS_FAILED(8, "common.error.validation"),
    ;

    private final int code;
    private final String messageKey;

    ErrorCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
