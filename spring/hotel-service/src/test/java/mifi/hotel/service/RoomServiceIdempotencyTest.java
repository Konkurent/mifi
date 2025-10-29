package mifi.hotel.service;

import mifi.hotel.entities.Hotel;
import mifi.hotel.entities.ProcessedRequest;
import mifi.hotel.entities.Room;
import mifi.hotel.repository.HotelRepository;
import mifi.hotel.repository.ProcessedRequestRepository;
import mifi.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomServiceIdempotencyTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ProcessedRequestRepository processedRequestRepository;

    @Autowired
    private HotelService hotelService;

    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        processedRequestRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();

        testHotel = Hotel.builder()
                .name("Test Hotel")
                .address("Test Address")
                .build();
        testHotel = hotelRepository.save(testHotel);

        testRoom = Room.builder()
                .hotel(testHotel)
                .number("101")
                .available(true)
                .timesBooked(0)
                .version(0)
                .build();
        testRoom = roomRepository.save(testRoom);
    }

    @Test
    void testIdempotency_MultipleCallsWithSameRequestId_NoDuplication() {
        // Given
        Long requestId = 123L;
        Long roomId = testRoom.getId();
        Integer initialCount = testRoom.getTimesBooked();

        // When - несколько вызовов с одним и тем же requestId
        roomService.incrementTimesBooked(requestId, roomId);
        roomService.incrementTimesBooked(requestId, roomId);
        roomService.incrementTimesBooked(requestId, roomId);

        // Then
        Room updatedRoom = roomRepository.findById(roomId).orElseThrow();
        assertEquals(initialCount + 1, updatedRoom.getTimesBooked(), 
                "Счетчик должен увеличиться только один раз");

        List<ProcessedRequest> processedRequests = processedRequestRepository
                .findAll()
                .stream()
                .filter(pr -> pr.getRequestId().equals(requestId))
                .toList();
        
        assertEquals(1, processedRequests.size(), 
                "Должна быть только одна запись о processed request");
    }

    @Test
    void testIdempotency_DifferentRequestIds_EachIncrements() {
        // Given
        Long requestId1 = 100L;
        Long requestId2 = 200L;
        Long requestId3 = 300L;
        Long roomId = testRoom.getId();
        Integer initialCount = testRoom.getTimesBooked();

        // When
        roomService.incrementTimesBooked(requestId1, roomId);
        roomService.incrementTimesBooked(requestId2, roomId);
        roomService.incrementTimesBooked(requestId3, roomId);

        // Then
        Room updatedRoom = roomRepository.findById(roomId).orElseThrow();
        assertEquals(initialCount + 3, updatedRoom.getTimesBooked(), 
                "Каждый уникальный requestId должен увеличить счетчик");
    }

    @Test
    void testDecrementIdempotency_MultipleCallsWithSameRequestId_NoDuplication() {
        // Given
        Long requestId = 400L;
        Long roomId = testRoom.getId();
        testRoom.setTimesBooked(5);
        roomRepository.save(testRoom);
        Integer initialCount = testRoom.getTimesBooked();

        // When - несколько вызовов с одним и тем же requestId
        roomService.decrementTimesBooked(requestId, roomId);
        roomService.decrementTimesBooked(requestId, roomId);
        roomService.decrementTimesBooked(requestId, roomId);

        // Then
        Room updatedRoom = roomRepository.findById(roomId).orElseThrow();
        assertEquals(initialCount - 1, updatedRoom.getTimesBooked(), 
                "Счетчик должен уменьшиться только один раз");

        List<ProcessedRequest> processedRequests = processedRequestRepository
                .findAll()
                .stream()
                .filter(pr -> pr.getRequestId().equals(requestId) && "DECREMENT".equals(pr.getOperationType()))
                .toList();
        
        assertEquals(1, processedRequests.size(), 
                "Должна быть только одна запись о processed request для DECREMENT");
    }
}

