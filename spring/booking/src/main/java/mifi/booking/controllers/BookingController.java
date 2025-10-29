package mifi.booking.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import mifi.booking.constant.BookingStatus;
import mifi.booking.converters.BookingDtoConverter;
import mifi.booking.dto.Booking;
import mifi.booking.dto.BookingFilter;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.services.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public Page<Booking> findAllByFilter(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BookingStatus status,
            @Valid @Min(0) @RequestParam(required = false) Integer page,
            @Valid @Min(0) @RequestParam(required = false) Integer size
    ) {
        return bookingService.getPageByFilter(
                BookingFilter.builder()
                        .userId(userId)
                        .roomId(roomId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(status)
                        .page(page)
                        .size(size)
                        .build()
        ).map(BookingDtoConverter::toDto);
    }

    @GetMapping("/{id}")
    public Booking getById(@Valid @NotNull @PathVariable Long id) {
        return BookingDtoConverter.toDto(bookingService.getById(id));
    }


    @DeleteMapping("/{id}")
    public void cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
    }


    @PostMapping
    public Booking bookRoom(@Valid @RequestBody CreateBookingPayload payload) {
        return BookingDtoConverter.toDto(bookingService.bookRoom(payload));
    }

}
