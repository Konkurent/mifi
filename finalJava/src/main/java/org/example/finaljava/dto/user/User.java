package org.example.finaljava.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Builder
public record User(String uuid, String login, String password) {
}
