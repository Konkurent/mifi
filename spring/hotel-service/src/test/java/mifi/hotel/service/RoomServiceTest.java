package mifi.hotel.service;

import mifi.hotel.dto.CreateRoomPayload;
import mifi.hotel.dto.RoomDto;
import mifi.hotel.entities.Hotel;
import mifi.hotel.entities.ProcessedRequest;
import mifi.hotel.entities.Room;
import mifi.hotel.exception.RoomNotFoundException;
import mifi.hotel.repository.ProcessedRequestRepository;
import mifi.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelService hotelService;

    @Mock
    private ProcessedRequestRepository processedRequestRepository;

    @InjectMocks
    private RoomService roomService;

    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .hotel(testHotel)
                .number("101")
                .available(true)
                .timesBooked(5)
                .version(0)
                .build();
    }

    @Test
    void testCreateRoom_Success() {
        // Given
        CreateRoomPayload payload = new CreateRoomPayload(1L, "102");
        when(hotelService.getHotelById(1L)).thenReturn(testHotel);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        // When
        RoomDto result = roomService.createRoom(payload);

        // Then
        assertNotNull(result);
        assertEquals(testRoom.getId(), result.getId());
        assertEquals(testRoom.getNumber(), result.getNumber());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetAvailableRooms_Success() {
        // Given
        List<Room> rooms = List.of(testRoom);
        when(roomRepository.findAllAvailableRooms()).thenReturn(rooms);

        // When
        List<RoomDto> result = roomService.getAvailableRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoom.getId(), result.get(0).getId());
    }

    @Test
    void testGetRecommendedRooms_Success() {
        // Given
        List<Room> rooms = List.of(testRoom);
        when(roomRepository.findAvailableRoomsOrderedByTimesBooked()).thenReturn(rooms);

        // When
        List<RoomDto> result = roomService.getRecommendedRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testResolveAvailableRoomId_Success() {
        // Given
        Long requestId = 100L;
        Set<Long> excludeRooms = Set.of(2L, 3L);
        List<Room> rooms = List.of(testRoom);
        when(roomRepository.findAllAvailableRooms()).thenReturn(rooms);

        // When
        Long result = roomService.resolveAvailableRoomId(requestId, excludeRooms);

        // Then
        assertNotNull(result);
        assertEquals(testRoom.getId(), result);
    }

    @Test
    void testResolveAvailableRoomId_NoAvailableRooms_ThrowsException() {
        // Given
        Long requestId = 100L;
        Set<Long> excludeRooms = Set.of();
        when(roomRepository.findAllAvailableRooms()).thenReturn(List.of());

        // When & Then
        assertThrows(RoomNotFoundException.class, () -> {
            roomService.resolveAvailableRoomId(requestId, excludeRooms);
        });
    }

    @Test
    void testIncrementTimesBooked_NewRequest_Success() {
        // Given
        Long requestId = 100L;
        Long roomId = 1L;
        when(processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT"))
                .thenReturn(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(processedRequestRepository.save(any(ProcessedRequest.class)))
                .thenReturn(new ProcessedRequest());

        // When
        roomService.incrementTimesBooked(requestId, roomId);

        // Then
        verify(roomRepository, times(1)).findById(roomId);
        verify(roomRepository, times(1)).save(testRoom);
        verify(processedRequestRepository, times(1)).save(any(ProcessedRequest.class));
        assertEquals(6, testRoom.getTimesBooked());
    }

    @Test
    void testIncrementTimesBooked_Idempotent_ReturnsEarly() {
        // Given
        Long requestId = 100L;
        Long roomId = 1L;
        when(processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT"))
                .thenReturn(true);

        // When
        roomService.incrementTimesBooked(requestId, roomId);

        // Then
        verify(processedRequestRepository, never()).save(any(ProcessedRequest.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testIncrementTimesBooked_OptimisticLockFailure_Retries() {
        // Given
        Long requestId = 100L;
        Long roomId = 1L;
        when(processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT"))
                .thenReturn(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"))
                .thenReturn(testRoom);
        when(processedRequestRepository.save(any(ProcessedRequest.class)))
                .thenReturn(new ProcessedRequest());

        // When
        assertDoesNotThrow(() -> {
            roomService.incrementTimesBooked(requestId, roomId);
        });

        // Then
        verify(roomRepository, atLeastOnce()).save(any(Room.class));
    }

    @Test
    void testGetRoomById_NotFound_ThrowsException() {
        // Given
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RoomNotFoundException.class, () -> {
            roomService.getRoomById(roomId);
        });
    }
}

