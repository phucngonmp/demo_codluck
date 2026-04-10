package org.example.demo.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    boolean success;
    int code;
    T data;
    Object errors;
    String message;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(errorCode.getCode());
        response.setData(null);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .data(null)
                .message(message)
                .errors(errors)
                .build();
    }
}
