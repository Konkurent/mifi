package mifi.hotel.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mifi.hotel.dto.CreateRoomPayload;
import mifi.hotel.dto.RoomDto;
import mifi.hotel.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomPayload payload) {
        RoomDto room = roomService.createRoom(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<RoomDto>> getAvailableRooms() {
        List<RoomDto> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/recommend")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<RoomDto>> getRecommendedRooms() {
        List<RoomDto> rooms = roomService.getRecommendedRooms();
        return ResponseEntity.ok(rooms);
    }

    // Endpoints для взаимодействия с booking-service
    @GetMapping("/v1/rooms/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Long> resolveAvailableRoomId(
            @RequestHeader("X-Request-Id") Long requestId,
            @RequestParam(value = "excludeRooms", required = false) Set<Long> excludeRooms) {
        log.info("Received resolveAvailableRoomId request: requestId={}, excludeRooms={}", requestId, excludeRooms);
        Set<Long> exclude = excludeRooms != null ? excludeRooms : Set.of();
        Long roomId = roomService.resolveAvailableRoomId(requestId, exclude);
        return ResponseEntity.ok(roomId);
    }

    @PutMapping("/v1/rooms/{roomId}/increment")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> incrementRoomUsage(
            @RequestHeader("X-Request-Id") Long requestId,
            @PathVariable Long roomId) {
        log.info("Received incrementRoomUsage request: requestId={}, roomId={}", requestId, roomId);
        roomService.incrementTimesBooked(requestId, roomId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/rooms/{roomId}/decrement")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> decrementRoomUsage(
            @RequestHeader("X-Request-Id") Long requestId,
            @PathVariable Long roomId) {
        log.info("Received decrementRoomUsage request: requestId={}, roomId={}", requestId, roomId);
        roomService.decrementTimesBooked(requestId, roomId);
        return ResponseEntity.ok().build();
    }
}

