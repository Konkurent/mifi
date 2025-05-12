package skillfactory.specialinstruments.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(
        Long id,
        @JsonProperty("first_name")
        String firstName,
        String username,
        String type
) {
}
