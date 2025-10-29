package mifi.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingPayload(
        @NotNull
        Long userId,

        Long roomId,

        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate,

        boolean autoSelect
) {

    public CreateBookingPayload(CreateBookingPayload payload, Long roomId) {
        this(payload.userId, roomId, payload.startDate(), payload.endDate(), payload.autoSelect());
    }

    @AssertTrue
    public boolean validateStartDate() {
        return startDate.isAfter(LocalDate.now()) && startDate.isBefore(endDate);
    }

    @AssertTrue
    public boolean validateEndDate() {
        return endDate.isAfter(LocalDate.now());
    }
}
