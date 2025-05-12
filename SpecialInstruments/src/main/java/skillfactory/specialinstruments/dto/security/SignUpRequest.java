package skillfactory.specialinstruments.dto.security;

public record SignUpRequest(
        String login,
        String password,
        String phone,
        String email
) {}
