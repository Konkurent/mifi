package mifi.booking.converters;

import lombok.experimental.UtilityClass;
import mifi.booking.dto.Booking;
import mifi.booking.entites.BookingEntity;

@UtilityClass
public class BookingDtoConverter {

    public Booking toDto(BookingEntity entity) {
        if (entity == null) return null;
        return Booking.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .roomId(entity.getRoomId())
                .status(entity.getStatus())
                .endDate(entity.getEndDate())
                .startDate(entity.getStartDate())
                .build();
    }

}
