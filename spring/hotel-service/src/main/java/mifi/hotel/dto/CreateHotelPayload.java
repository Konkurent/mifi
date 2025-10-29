package mifi.hotel.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelPayload(
        @NotBlank(message = "Hotel name cannot be blank")
        String name,
        @NotBlank(message = "Hotel address cannot be blank")
        String address
) {
}

