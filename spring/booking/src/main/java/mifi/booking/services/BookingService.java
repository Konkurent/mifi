package mifi.booking.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mifi.booking.constant.BookingStatus;
import mifi.booking.controllers.advise.SystemCode;
import mifi.booking.dto.BookingFilter;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.entites.BookingEntity;
import mifi.booking.exception.BookingRuntimeException;
import mifi.booking.repository.BookingRepository;
import mifi.booking.repository.BookingSequence;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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
        Long rqId = bookingSequence.next();
        log.info("Starting booking process: rqId={}, userId={}, roomId={}, autoSelect={}", 
                rqId, payload.userId(), payload.roomId(), payload.autoSelect());
        
        if (!payload.autoSelect()) {
            assertRoomExisting(payload.roomId());
            return bookRoom(payload, payload.roomId(), rqId);
        } else {
            Set<Long> excludeRooms = bookingRepository.getAllByFilter(
                    BookingFilter.builder()
                            .startDate(payload.startDate())
                            .endDate(payload.endDate())
                            .excludeStatus(new BookingStatus[]{ BookingStatus.CANCELLED })
                            .build()
            ).stream().map(BookingEntity::getRoomId).collect(Collectors.toSet());
            Long roomId = hotelService.resolveAvailableRoomId(rqId, excludeRooms);
            log.info("Resolved available room: rqId={}, roomId={}", rqId, roomId);
            return bookRoom(payload, roomId, rqId);
        }
    }

    private void assertRoomExisting(Long roomId) {
        if (roomId == null) {
            throw new BookingRuntimeException("Room is null", SystemCode.NOT_FOUND);
        }
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public BookingEntity bookRoom(CreateBookingPayload payload, @NonNull Long roomId, Long rqId) {
        Long bookingId = null;
        BookingEntity entity = null;
        boolean incrementCompleted = false;
        
        try {
            // Проверка доступности комнаты
            assertRoomFreedom(roomId, payload.startDate(), payload.endDate());
            
            // Создание бронирования в статусе PENDING
            entity = BookingEntity.builder()
                    .roomId(roomId)
                    .user(userService.getUserById(payload.userId()))
                    .rqId(rqId)
                    .status(BookingStatus.PENDING)
                    .startDate(payload.startDate())
                    .endDate(payload.endDate())
                    .build();
            entity = bookingRepository.save(entity);
            bookingId = entity.getId();
            log.info("Booking created: bookingId={}, rqId={}, roomId={}, status=PENDING", 
                    bookingId, rqId, roomId);
            
            // Вызов hotel-service для инкремента счетчика
            hotelService.incrementRoomUsage(rqId, roomId);
            incrementCompleted = true;
            log.info("Room usage incremented: bookingId={}, rqId={}, roomId={}", 
                    bookingId, rqId, roomId);
            
            // Переход в CONFIRMED при успехе
            entity.setStatus(BookingStatus.CONFIRMED);
            entity = bookingRepository.save(entity);
            log.info("Booking confirmed: bookingId={}, rqId={}, roomId={}, status=CONFIRMED", 
                    bookingId, rqId, roomId);
            
            return entity;
            
        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic locking failure: bookingId={}, rqId={}, roomId={}", 
                    bookingId, rqId, roomId, e);
            // Компенсация при оптимистичной блокировке
            performCompensation(bookingId, entity, rqId, roomId, incrementCompleted, e);
            throw new BookingRuntimeException("Concurrent modification detected, please retry", SystemCode.ALREADY_EXIST);
            
        } catch (Exception e) {
            log.error("Error during booking: bookingId={}, rqId={}, roomId={}", 
                    bookingId, rqId, roomId, e);
            // Компенсация: откат изменений
            performCompensation(bookingId, entity, rqId, roomId, incrementCompleted, e);
            throw new BookingRuntimeException("Failed to complete booking: " + e.getMessage(), SystemCode.NOT_FOUND);
        }
    }

    /**
     * Компенсация при ошибке бронирования
     * Откатывает все выполненные действия: уменьшает счетчик в hotel-service и переводит бронирование в CANCELLED
     */
    private void performCompensation(Long bookingId, BookingEntity entity, Long rqId, Long roomId, 
                                     boolean incrementCompleted, Exception originalError) {
        // Компенсация инкремента счетчика в hotel-service
        if (incrementCompleted && roomId != null && rqId != null) {
            try {
                log.info("Compensating increment: bookingId={}, rqId={}, roomId={}", 
                        bookingId, rqId, roomId);
                hotelService.decrementRoomUsage(rqId, roomId);
                log.info("Room usage decremented as compensation: bookingId={}, rqId={}, roomId={}", 
                        bookingId, rqId, roomId);
            } catch (Exception decrementError) {
                log.error("Failed to decrement room usage during compensation: bookingId={}, rqId={}, roomId={}", 
                        bookingId, rqId, roomId, decrementError);
            }
        }
        
        // Компенсация: переводим в CANCELLED
        if (entity != null && bookingId != null) {
            try {
                entity.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(entity);
                log.info("Booking cancelled due to error: bookingId={}, rqId={}, roomId={}, status=CANCELLED", 
                        bookingId, rqId, roomId);
            } catch (Exception compensationError) {
                log.error("Failed to cancel booking during compensation: bookingId={}, rqId={}", 
                        bookingId, rqId, compensationError);
            }
        }
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
            log.warn("Room already booked: roomId={}, startDate={}, endDate={}", 
                    roomId, startDate, endDate);
            throw new BookingRuntimeException("Booking already exist!", SystemCode.ALREADY_EXIST);
        }
    }

    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking: bookingId={}", bookingId);
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingRuntimeException("Booking not found", SystemCode.NOT_FOUND));
        
        // Компенсация: уменьшаем счетчик в hotel-service для подтвержденных бронирований
        if (entity.getStatus() == BookingStatus.CONFIRMED && entity.getRoomId() != null && entity.getRqId() != null) {
            try {
                log.info("Decrementing room usage for cancelled booking: bookingId={}, rqId={}, roomId={}", 
                        bookingId, entity.getRqId(), entity.getRoomId());
                hotelService.decrementRoomUsage(entity.getRqId(), entity.getRoomId());
                log.info("Room usage decremented: bookingId={}, rqId={}, roomId={}", 
                        bookingId, entity.getRqId(), entity.getRoomId());
            } catch (Exception e) {
                log.error("Failed to decrement room usage during cancellation: bookingId={}, rqId={}, roomId={}", 
                        bookingId, entity.getRqId(), entity.getRoomId(), e);
                // Продолжаем отмену бронирования даже если не удалось уменьшить счетчик
            }
        }
        
        entity.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(entity);
        log.info("Booking cancelled: bookingId={}, rqId={}", bookingId, entity.getRqId());
    }
}
