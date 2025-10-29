package mifi.booking.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
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

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserService userService;
    private final HotelService hotelService;
    private final BookingSequence bookingSequence;
    private final BookingRepository bookingRepository;

    public Page<BookingEntity> getPageByFilter(BookingFilter filter) {
        return bookingRepository.getPageByFilter(filter);
    }

    public BookingEntity getById(@Valid @NotNull Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingRuntimeException("Booking not found!", SystemCode.NOT_FOUND));
    }

    public BookingEntity bookRoom(CreateBookingPayload payload) {
        if (!payload.autoSelect()) {
            assertRoomExisting(payload.roomId());
            return bookRoom(payload, payload.roomId(), bookingSequence.next());
        } else {
            Long rqId = bookingSequence.next();
            Set<Long> excludeRooms = bookingRepository.getAllByFilter(
                    BookingFilter.builder()
                            .roomId(payload.roomId())
                            .startDate(payload.startDate())
                            .endDate(payload.endDate())
                            .excludeStatus(new BookingStatus[]{ BookingStatus.CANCELLED })
                            .build()
            ).stream().map(BookingEntity::getRoomId).collect(Collectors.toSet());
            Long roomId = hotelService.resolveAvailableRoomId(rqId, excludeRooms);
            return bookRoom(payload, roomId, rqId);
        }
    }

    private void assertRoomExisting(Long roomId) {
        if (roomId == null) {
            throw new BookingRuntimeException("Room is null", SystemCode.NOT_FOUND);
        }
    }

    public BookingEntity bookRoom(CreateBookingPayload payload, @NonNull Long roomId, Long rqId) {
        assertRoomFreedom(payload.roomId(), payload.startDate(), payload.endDate());
        BookingEntity entity = BookingEntity.builder()
                .roomId(payload.roomId())
                .user(userService.getUserById(payload.userId()))
                .rqId(rqId)
                .status(BookingStatus.PENDING)
                .startDate(payload.startDate())
                .endDate(payload.endDate())
                .build();
        bookingRepository.save(entity);
        hotelService.incrementRoomUsage(rqId, roomId);
        entity.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(entity);
        return entity;
    }

    private void assertRoomFreedom(Long roomId, LocalDate startDate, LocalDate endDate) {
        Page<BookingEntity> page = getPageByFilter(
                BookingFilter.builder()
                        .roomId(roomId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .excludeStatus(new BookingStatus[]{ BookingStatus.CANCELLED })
                        .size(1)
                        .build()
        );
        if (!page.isEmpty()) {
            throw new BookingRuntimeException("Booking already exist!", SystemCode.ALREADY_EXIST);
        }
    }

    public void cancelBooking(Long bookingId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingRuntimeException("Booking not found", SystemCode.NOT_FOUND));
        entity.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(entity);
    }
}
