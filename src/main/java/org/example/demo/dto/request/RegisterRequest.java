package org.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "tên tài khoản không được để trống")
        String username,

        @Email(message = "email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Pattern(
                regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=.*\\d).+$",
                message = "Mật khẩu phải chứa ít nhất 1 số và 1 ký tự đặc biệt"
        )
        String password,

        @NotBlank(message = "Vui lòng nhập lại mật khẩu")
        String confirmPassword
) {
}
