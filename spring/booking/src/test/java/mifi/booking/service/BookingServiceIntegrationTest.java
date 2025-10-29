package mifi.booking.service;

import mifi.booking.constant.BookingStatus;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.entites.BookingEntity;
import mifi.booking.entites.UserEntity;
import mifi.booking.exception.BookingRuntimeException;
import mifi.booking.repository.BookingRepository;
import mifi.booking.repository.UserRepository;
import mifi.booking.services.BookingService;
import mifi.booking.services.HotelService;
import mifi.booking.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    private UserEntity testUser;
    private CreateBookingPayload validPayload;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        testUser = UserEntity.builder()
                .email("test@example.com")
                .login("testuser")
                .firstName("Test")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);

        validPayload = new CreateBookingPayload(
                testUser.getId(),
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                false
        );

        doNothing().when(hotelService).incrementRoomUsage(any(Long.class), eq(1L));
    }

    @Test
    void testBookRoom_SuccessfulFlow_PendingToConfirmed() {
        // Given
        doNothing().when(hotelService).incrementRoomUsage(any(Long.class), eq(1L));

        // When
        BookingEntity result = bookingService.bookRoom(validPayload);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getRqId());
        verify(hotelService, times(1)).incrementRoomUsage(any(Long.class), eq(1L));
    }

    @Test
    void testBookRoom_HotelServiceFailure_CompensatesToCancelled() {
        // Given
        when(hotelService.incrementRoomUsage(any(Long.class), eq(1L)))
                .thenThrow(new RuntimeException("Hotel service unavailable"));

        // When
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.bookRoom(validPayload);
        });

        // Then - проверяем компенсацию
        Optional<BookingEntity> cancelledBooking = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .findFirst();

        assertTrue(cancelledBooking.isPresent(), 
                "При ошибке бронирование должно быть переведено в статус CANCELLED");
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.get().getStatus());
    }

    @Test
    void testBookRoom_AutoSelect_CallsHotelService() {
        // Given
        CreateBookingPayload autoSelectPayload = new CreateBookingPayload(
                testUser.getId(),
                null,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                true
        );
        Long resolvedRoomId = 2L;
        when(hotelService.resolveAvailableRoomId(any(Long.class), any()))
                .thenReturn(resolvedRoomId);
            doNothing().when(hotelService).incrementRoomUsage(any(Long.class), eq(resolvedRoomId));

        // When
        BookingEntity result = bookingService.bookRoom(autoSelectPayload);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(hotelService, times(1)).resolveAvailableRoomId(any(Long.class), any());
        verify(hotelService, times(1)).incrementRoomUsage(any(Long.class), eq(resolvedRoomId));
    }

    @Test
    void testBookRoom_ConcurrentBooking_PreventsDuplicate() {
        // Given
        doNothing().when(hotelService).incrementRoomUsage(any(Long.class), eq(1L));

        // When - первое бронирование
        BookingEntity first = bookingService.bookRoom(validPayload);
        assertEquals(BookingStatus.CONFIRMED, first.getStatus());

        // Попытка забронировать ту же комнату на те же даты
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.bookRoom(validPayload);
        });
    }

    @Test
    void testCancelBooking_Success() {
        // Given
        doNothing().when(hotelService).incrementRoomUsage(any(Long.class), eq(1L));
        BookingEntity booking = bookingService.bookRoom(validPayload);
        Long bookingId = booking.getId();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // When
        bookingService.cancelBooking(bookingId);

        // Then
        Optional<BookingEntity> cancelled = bookingRepository.findById(bookingId);
        assertTrue(cancelled.isPresent());
        assertEquals(BookingStatus.CANCELLED, cancelled.get().getStatus());
    }
}

