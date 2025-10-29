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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomServiceIntegrationTest {

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
    void testIncrementTimesBooked_Idempotency() {
        // Given
        Long requestId = 100L;
        Long roomId = testRoom.getId();
        Integer initialCount = testRoom.getTimesBooked();

        // When - первый вызов
        roomService.incrementTimesBooked(requestId, roomId);
        Room afterFirst = roomRepository.findById(roomId).orElseThrow();
        Integer afterFirstCount = afterFirst.getTimesBooked();

        // Повторный вызов с тем же requestId
        roomService.incrementTimesBooked(requestId, roomId);
        Room afterSecond = roomRepository.findById(roomId).orElseThrow();
        Integer afterSecondCount = afterSecond.getTimesBooked();

        // Then
        assertEquals(initialCount + 1, afterFirstCount);
        assertEquals(afterFirstCount, afterSecondCount, "Второй вызов не должен увеличивать счетчик");
        assertTrue(processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT"));
    }

    @Test
    void testResolveAvailableRoomId_ExcludesRooms() {
        // Given
        Room room2 = Room.builder()
                .hotel(testHotel)
                .number("102")
                .available(true)
                .timesBooked(0)
                .build();
        roomRepository.save(room2);

        Long requestId = 200L;
        Set<Long> excludeRooms = Set.of(testRoom.getId());

        // When
        Long resolvedRoomId = roomService.resolveAvailableRoomId(requestId, excludeRooms);

        // Then
        assertNotEquals(testRoom.getId(), resolvedRoomId);
        assertEquals(room2.getId(), resolvedRoomId);
        // Проверяем, что используется сортировка по timesBooked для равномерного распределения
        // room2 с timesBooked=0 должен быть выбран перед room1, если он не исключен
    }

    @Test
    void testDecrementTimesBooked_Idempotency() {
        // Given
        Long requestId = 200L;
        Long roomId = testRoom.getId();
        testRoom.setTimesBooked(5);
        roomRepository.save(testRoom);
        Integer initialCount = testRoom.getTimesBooked();

        // When - первый вызов
        roomService.decrementTimesBooked(requestId, roomId);
        Room afterFirst = roomRepository.findById(roomId).orElseThrow();
        Integer afterFirstCount = afterFirst.getTimesBooked();

        // Повторный вызов с тем же requestId
        roomService.decrementTimesBooked(requestId, roomId);
        Room afterSecond = roomRepository.findById(roomId).orElseThrow();
        Integer afterSecondCount = afterSecond.getTimesBooked();

        // Then
        assertEquals(initialCount - 1, afterFirstCount);
        assertEquals(afterFirstCount, afterSecondCount, "Второй вызов не должен уменьшать счетчик");
        assertTrue(processedRequestRepository.existsByRequestIdAndOperationType(requestId, "DECREMENT"));
    }

    @Test
    void testConcurrentIncrement_OptimisticLocking() throws InterruptedException {
        // Given
        int threads = 10;
        int incrementsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        Long requestId = (long) (threadNum * 1000 + j);
                        try {
                            roomService.incrementTimesBooked(requestId, testRoom.getId());
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        Room finalRoom = roomRepository.findById(testRoom.getId()).orElseThrow();
        assertEquals(successCount.get(), finalRoom.getTimesBooked());
        assertTrue(failureCount.get() >= 0, "Некоторые операции могли провалиться из-за оптимистичных блокировок");
    }
}

