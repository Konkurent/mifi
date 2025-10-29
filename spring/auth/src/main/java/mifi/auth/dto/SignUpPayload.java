package mifi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SignUpPayload(
        @NotBlank
        @Length(min = 1)
        String firstName,

        String middleName,

        @NotBlank
        @Length(min = 1)
        String lastName,

        @Email
        @NotBlank
        String email,

        @Length(min = 3)
        String login,

        @NotBlank
        String password
) {
}
