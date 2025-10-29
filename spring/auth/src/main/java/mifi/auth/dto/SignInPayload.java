package mifi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInPayload(

        @NotBlank
        String userName,

        @NotBlank
        String password

) {
}
