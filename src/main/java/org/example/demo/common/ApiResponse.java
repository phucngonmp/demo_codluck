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
    String message;

    // Thành công với dữ liệu
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setData(data);
        response.setMessage(message);
        return response;
    }
    // Thất bại với thông điệp lỗi và mã lỗi tùy chỉnh
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(errorCode.getCode());
        response.setData(null);
        response.setMessage(errorCode.getMessage());
        return response;
    }
}


