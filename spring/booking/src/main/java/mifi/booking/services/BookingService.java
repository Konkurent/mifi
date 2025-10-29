package mifi.booking.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import mifi.booking.constant.BookingStatus;
import mifi.booking.controllers.advise.SystemCode;
import mifi.booking.dto.BookingFilter;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.entites.BookingEntity;
import mifi.booking.exception.BookingRuntimeException;
import mifi.booking.repository.BookingRepository;
import mifi.booking.repository.BookingSequence;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserService userService;
    private final BookingSequence bookingSequence;
    private final BookingRepository bookingRepository;

    public Page<BookingEntity> getPageByFilter(BookingFilter filter) {
        return bookingRepository.getPageByFilter(filter);
    }

    public BookingEntity getById(@Valid @NotNull Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingRuntimeException("Booking not found!", SystemCode.NOT_FOUND));
    }


    public BookingEntity bookRoom(CreateBookingPayload payload) {
        assertRoomFreedom(payload.roomId(), payload.startDate(), payload.endDate());
        BookingEntity.builder()
                .roomId(payload.roomId())
                .user(userService.getUserById(payload.userId()))
                .status(BookingStatus.PENDING)
                .build();
    }

    private void assertRoomFreedom(Long roomId, LocalDate startDate, LocalDate endDate) {
        Page<BookingEntity> page = getPageByFilter(
                BookingFilter.builder()
                        .roomId(roomId)
                        .startDate(startDate)
                        .endDate(endDate)

                        .size(1)
                        .build()
        );
        if (!page.isEmpty()) {
            throw new BookingRuntimeException("Booking already exist!", SystemCode.ALREADY_EXIST);
        }
    }
}
