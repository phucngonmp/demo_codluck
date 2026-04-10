package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{validation.login.identifier.required}")
        String identifier,

        @NotBlank(message = "{validation.login.password.required}")
        String password
) {
}
