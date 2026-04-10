package org.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "{validation.register.username.required}")
        String username,

        @NotBlank(message = "{validation.register.email.required}")
        @Email(message = "{validation.register.email.invalid}")
        String email,

        @NotBlank(message = "{validation.register.password.required}")
        @Pattern(
                regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=.*\\d).+$",
                message = "{validation.register.password.pattern}"
        )
        String password,

        @NotBlank(message = "{validation.register.confirmPassword.required}")
        String confirmPassword
) {
}
