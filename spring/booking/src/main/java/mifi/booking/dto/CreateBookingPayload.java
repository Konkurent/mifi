package mifi.booking.dto;

import java.time.LocalDate;

public record CreateBookingPayload(
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        boolean autoSelect
) {  }
