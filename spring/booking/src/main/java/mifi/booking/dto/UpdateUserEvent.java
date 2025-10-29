package mifi.booking.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdateUserEvent(
        LocalDateTime updateTime,
        String email,
        String login,
        Long userId
) {
}
