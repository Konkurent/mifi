package mifi.auth.dto;


public record User(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String email,
        String login
) {
}
