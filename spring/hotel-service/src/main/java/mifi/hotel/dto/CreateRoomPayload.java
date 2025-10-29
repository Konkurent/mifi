package mifi.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomPayload(
        @NotNull(message = "Hotel ID cannot be null")
        Long hotelId,
        @NotBlank(message = "Room number cannot be blank")
        String number
) {
}

