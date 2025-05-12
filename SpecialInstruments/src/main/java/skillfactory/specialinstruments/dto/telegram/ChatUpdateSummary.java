package skillfactory.specialinstruments.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatUpdateSummary(
        @JsonProperty("update_id")
        Long updateId,
        Message message
) {
}
