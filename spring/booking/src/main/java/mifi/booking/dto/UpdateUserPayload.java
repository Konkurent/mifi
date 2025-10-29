package mifi.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserPayload(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String middleName,

        @Email
        @NotBlank
        String email,
        String login,

        @NotNull
        Long userId
) {
}
