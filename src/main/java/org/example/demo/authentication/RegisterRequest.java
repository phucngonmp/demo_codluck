package org.example.demo.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "validation.email.notBlank")
    @Email(message = "validation.email.invalid")
    @Size(max = 100, message = "validation.email.maxSize")
    private String email;
    @NotBlank(message = "validation.password.notBlank")
    @Size(min = 8, max = 32, message = "validation.password.size")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,32}$",
            message = "validation.password.pattern"
    )
    private String password;
}
