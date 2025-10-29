package mifi.booking.service;

import mifi.booking.constant.BookingStatus;
import mifi.booking.controllers.advise.SystemCode;
import mifi.booking.dto.BookingFilter;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.entites.BookingEntity;
import mifi.booking.entites.UserEntity;
import mifi.booking.exception.BookingRuntimeException;
import mifi.booking.repository.BookingRepository;
import mifi.booking.repository.BookingSequence;
import mifi.booking.services.HotelService;
import mifi.booking.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private HotelService hotelService;

    @Mock
    private BookingSequence bookingSequence;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private CreateBookingPayload validPayload;
    private UserEntity testUser;
    private BookingEntity testBooking;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .login("testuser")
                .build();

        validPayload = new CreateBookingPayload(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                false
        );

        testBooking = BookingEntity.builder()
                .id(1L)
                .roomId(1L)
                .user(testUser)
                .startDate(validPayload.startDate())
                .endDate(validPayload.endDate())
                .status(BookingStatus.PENDING)
                .rqId(100L)
                .version(0)
                .build();
    }

    @Test
    void testBookRoom_Success_TransitionsPendingToConfirmed() {
        // Given
        Long rqId = 100L;
        Long roomId = 1L;
        when(bookingSequence.next()).thenReturn(rqId);
        when(bookingRepository.getPageByFilter(any(BookingFilter.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);
        doNothing().when(hotelService).incrementRoomUsage(eq(rqId), eq(roomId));

        // When
        BookingEntity result = bookingService.bookRoom(validPayload);

        // Then
        assertNotNull(result);
        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository, atLeastOnce()).save(captor.capture());
        
        BookingEntity saved = captor.getAllValues().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .findFirst()
                .orElse(null);
        
        assertNotNull(saved, "Бронирование должно быть в статусе CONFIRMED");
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
        verify(hotelService, times(1)).incrementRoomUsage(rqId, roomId);
    }

    @Test
    void testBookRoom_RoomAlreadyBooked_ThrowsException() {
        // Given
        Long rqId = 100L;
        when(bookingSequence.next()).thenReturn(rqId);
        when(bookingRepository.getPageByFilter(any(BookingFilter.class)))
                .thenReturn(org.springframework.data.domain.Page.of(java.util.List.of(testBooking)));

        // When & Then
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.bookRoom(validPayload);
        });
        verify(hotelService, never()).incrementRoomUsage(any(), any());
    }

    @Test
    void testBookRoom_HotelServiceFails_CompensatesWithCancelled() {
        // Given
        Long rqId = 100L;
        Long roomId = 1L;
        when(bookingSequence.next()).thenReturn(rqId);
        when(bookingRepository.getPageByFilter(any(BookingFilter.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(bookingRepository.save(any(BookingEntity.class)))
                .thenReturn(testBooking)
                .thenReturn(testBooking); // для компенсации
        doThrow(new RuntimeException("Hotel service unavailable"))
                .when(hotelService).incrementRoomUsage(eq(rqId), eq(roomId));

        // When
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.bookRoom(validPayload);
        });

        // Then - проверяем компенсацию
        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository, atLeast(2)).save(captor.capture());
        
        BookingEntity cancelled = captor.getAllValues().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .findFirst()
                .orElse(null);
        
        assertNotNull(cancelled, "При ошибке бронирование должно быть в статусе CANCELLED");
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testBookRoom_OptimisticLockFailure_ThrowsException() {
        // Given
        Long rqId = 100L;
        when(bookingSequence.next()).thenReturn(rqId);
        when(bookingRepository.getPageByFilter(any(BookingFilter.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(bookingRepository.save(any(BookingEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"));

        // When & Then
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.bookRoom(validPayload);
        });
    }

    @Test
    void testBookRoom_AutoSelect_CallsHotelService() {
        // Given
        Long rqId = 100L;
        Long resolvedRoomId = 2L;
        CreateBookingPayload autoSelectPayload = new CreateBookingPayload(
                1L,
                null,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                true
        );
        when(bookingSequence.next()).thenReturn(rqId);
        when(bookingRepository.getAllByFilter(any(BookingFilter.class)))
                .thenReturn(List.of());
        when(hotelService.resolveAvailableRoomId(eq(rqId), any(Set.class)))
                .thenReturn(resolvedRoomId);
        when(bookingRepository.getPageByFilter(any(BookingFilter.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);
        doNothing().when(hotelService).incrementRoomUsage(eq(rqId), eq(resolvedRoomId));

        // When
        BookingEntity result = bookingService.bookRoom(autoSelectPayload);

        // Then
        assertNotNull(result);
        verify(hotelService, times(1)).resolveAvailableRoomId(eq(rqId), any(Set.class));
        verify(hotelService, times(1)).incrementRoomUsage(rqId, resolvedRoomId);
    }

    @Test
    void testCancelBooking_Success() {
        // Given
        Long bookingId = 1L;
        testBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);

        // When
        bookingService.cancelBooking(bookingId);

        // Then
        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository, times(1)).save(captor.capture());
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void testCancelBooking_NotFound_ThrowsException() {
        // Given
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookingRuntimeException.class, () -> {
            bookingService.cancelBooking(bookingId);
        });
    }
}

