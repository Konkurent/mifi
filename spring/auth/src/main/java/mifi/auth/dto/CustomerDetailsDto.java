package mifi.auth.dto;

import java.util.List;

public record CustomerDetailsDto(
        String email,
        Long userId,
        List<String> authorities
) {
}

