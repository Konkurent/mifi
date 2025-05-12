package skillfactory.specialinstruments.dto.accounts;

import lombok.Builder;
import org.springframework.lang.Nullable;
import skillfactory.specialinstruments.constants.Role;

@Builder
public record AccountFilter(
        @Nullable String login,
        @Nullable Role role,
        Integer page,
        Integer size
) { }
