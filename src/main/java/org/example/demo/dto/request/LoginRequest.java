package org.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "identifier không được để trống")
        String identifier,
        @NotBlank(message = "password không được để trống")
        String password
) {}
