package mifi.booking.dto;

import lombok.Builder;
import mifi.booking.constant.BookingStatus;

import java.time.LocalDate;

@Builder
public record Booking(
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status
) {}
