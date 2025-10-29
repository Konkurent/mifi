package mifi.auth.dto;

import java.time.LocalDateTime;

public record UpdateUserPayload(
        LocalDateTime updateTime,
        String email,
        String login,
        Long userId
) {
}
