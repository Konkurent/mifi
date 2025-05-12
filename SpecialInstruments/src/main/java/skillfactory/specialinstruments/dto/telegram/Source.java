package skillfactory.specialinstruments.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Source(
        Long id,
        @JsonProperty("is_bot")
        Boolean bot,
        @JsonProperty("first_name")
        String firstName,
        String username,
        @JsonProperty("language_code")
        String languageCode
) {
}
