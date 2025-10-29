package mifi.booking.dto;

import lombok.Builder;
import lombok.Data;
import mifi.booking.constant.BookingStatus;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BookingFilter {

    @Nullable
    private Long userId;

    @Nullable
    private Long roomId;

    @Nullable
    private LocalDate startDate;

    @Nullable
    private LocalDate endDate;

    @Nullable
    private BookingStatus[] excludeStatus;

    @Nullable
    private BookingStatus status;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

}
