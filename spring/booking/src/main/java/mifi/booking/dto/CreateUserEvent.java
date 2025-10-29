package mifi.booking.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateUserEvent(
        LocalDateTime creationDate,
        String password,
        Long userId,
        String email,
        String login,
        Boolean admin
) {
}
