package skillfactory.specialinstruments.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Updates(Boolean ok, List<ChatUpdateSummary> result) {

}



