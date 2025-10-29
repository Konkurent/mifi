package mifi.hotel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private String number;
    private Boolean available;
    private Integer timesBooked;
}

