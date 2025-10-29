package mifi.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateUserPayload(
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
