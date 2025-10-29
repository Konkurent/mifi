package mifi.hotel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mifi.hotel.dto.CreateRoomPayload;
import mifi.hotel.dto.RoomDto;
import mifi.hotel.entities.Hotel;
import mifi.hotel.entities.ProcessedRequest;
import mifi.hotel.entities.Room;
import mifi.hotel.exception.RoomNotFoundException;
import mifi.hotel.repository.ProcessedRequestRepository;
import mifi.hotel.repository.RoomRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelService hotelService;
    private final ProcessedRequestRepository processedRequestRepository;

    public RoomDto createRoom(CreateRoomPayload payload) {
        Hotel hotel = hotelService.getHotelById(payload.hotelId());
        Room room = Room.builder()
                .hotel(hotel)
                .number(payload.number())
                .available(true)
                .timesBooked(0)
                .build();
        Room saved = roomRepository.save(room);
        return toDto(saved);
    }

    public List<RoomDto> getAvailableRooms() {
        return roomRepository.findAllAvailableRooms().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RoomDto> getRecommendedRooms() {
        return roomRepository.findAvailableRoomsOrderedByTimesBooked().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + id));
    }

    /**
     * Идемпотентное увеличение счетчика бронирований с оптимистичной блокировкой
     */
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void incrementTimesBooked(Long requestId, Long roomId) {
        log.info("Processing increment request: requestId={}, roomId={}", requestId, roomId);
        
        // Проверка идемпотентности
        if (processedRequestRepository.existsByRequestIdAndOperationType(requestId, "INCREMENT")) {
            log.info("Request already processed: requestId={}, roomId={}", requestId, roomId);
            return; // Идемпотентность - запрос уже обработан
        }

        try {
            Room room = getRoomById(roomId);
            room.setTimesBooked(room.getTimesBooked() + 1);
            roomRepository.save(room);

            // Сохраняем информацию о processed request
            ProcessedRequest processedRequest = ProcessedRequest.builder()
                    .requestId(requestId)
                    .roomId(roomId)
                    .operationType("INCREMENT")
                    .processedAt(LocalDateTime.now())
                    .build();
            processedRequestRepository.save(processedRequest);
            
            log.info("Successfully incremented timesBooked: requestId={}, roomId={}, newCount={}", 
                    requestId, roomId, room.getTimesBooked());
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure, retrying: requestId={}, roomId={}", requestId, roomId);
            throw e;
        }
    }

    /**
     * Идемпотентное уменьшение счетчика бронирований с оптимистичной блокировкой
     * Используется для компенсации при отмене бронирования
     */
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void decrementTimesBooked(Long requestId, Long roomId) {
        log.info("Processing decrement request: requestId={}, roomId={}", requestId, roomId);
        
        // Проверка идемпотентности
        if (processedRequestRepository.existsByRequestIdAndOperationType(requestId, "DECREMENT")) {
            log.info("Request already processed: requestId={}, roomId={}", requestId, roomId);
            return; // Идемпотентность - запрос уже обработан
        }

        try {
            Room room = getRoomById(roomId);
            if (room.getTimesBooked() > 0) {
                room.setTimesBooked(room.getTimesBooked() - 1);
                roomRepository.save(room);

                // Сохраняем информацию о processed request
                ProcessedRequest processedRequest = ProcessedRequest.builder()
                        .requestId(requestId)
                        .roomId(roomId)
                        .operationType("DECREMENT")
                        .processedAt(LocalDateTime.now())
                        .build();
                processedRequestRepository.save(processedRequest);
                
                log.info("Successfully decremented timesBooked: requestId={}, roomId={}, newCount={}", 
                        requestId, roomId, room.getTimesBooked());
            } else {
                log.warn("Cannot decrement timesBooked below zero: requestId={}, roomId={}", requestId, roomId);
            }
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure, retrying: requestId={}, roomId={}", requestId, roomId);
            throw e;
        }
    }

    /**
     * Получение доступной комнаты для бронирования с исключением уже забронированных
     * Выбирает номер с наименьшим количеством бронирований для равномерного распределения нагрузки
     */
    public Long resolveAvailableRoomId(Long requestId, Set<Long> excludeRooms) {
        log.info("Resolving available room: requestId={}, excludeRooms={}", requestId, excludeRooms);
        
        List<Room> availableRooms = roomRepository.findAvailableRoomsOrderedByTimesBooked();
        Room selectedRoom = availableRooms.stream()
                .filter(room -> !excludeRooms.contains(room.getId()))
                .findFirst()
                .orElseThrow(() -> new RoomNotFoundException("No available rooms found"));

        log.info("Resolved room: requestId={}, roomId={}, timesBooked={}", 
                requestId, selectedRoom.getId(), selectedRoom.getTimesBooked());
        return selectedRoom.getId();
    }

    private RoomDto toDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .hotelName(room.getHotel().getName())
                .number(room.getNumber())
                .available(room.getAvailable())
                .timesBooked(room.getTimesBooked())
                .build();
    }
}

