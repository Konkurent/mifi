package mifi.booking.dto;


import lombok.Builder;

@Builder
public record User(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String email,
        String login
) {
}
