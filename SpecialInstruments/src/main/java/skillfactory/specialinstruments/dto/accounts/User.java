package skillfactory.specialinstruments.dto.accounts;

import lombok.Builder;
import skillfactory.specialinstruments.constants.Role;

import java.time.LocalDateTime;

@Builder
public record User(
    String login,
    Role role,
    LocalDateTime createDateTime,
    LocalDateTime updateDateTime
) {}
