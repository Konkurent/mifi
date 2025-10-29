package mifi.hotel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mifi.hotel.dto.CreateRoomPayload;
import mifi.hotel.dto.RoomDto;
import mifi.hotel.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoomDto testRoomDto;
    private CreateRoomPayload validPayload;

    @BeforeEach
    void setUp() {
        testRoomDto = RoomDto.builder()
                .id(1L)
                .hotelId(1L)
                .hotelName("Test Hotel")
                .number("101")
                .available(true)
                .timesBooked(5)
                .build();

        validPayload = new CreateRoomPayload(1L, "102");
    }

    @Test
    void testCreateRoom_Success() throws Exception {
        // Given
        when(roomService.createRoom(any(CreateRoomPayload.class))).thenReturn(testRoomDto);

        // When & Then
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("101"));
    }

    @Test
    void testGetAvailableRooms_Success() throws Exception {
        // Given
        List<RoomDto> rooms = List.of(testRoomDto);
        when(roomService.getAvailableRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetRecommendedRooms_Success() throws Exception {
        // Given
        List<RoomDto> rooms = List.of(testRoomDto);
        when(roomService.getRecommendedRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/api/rooms/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testResolveAvailableRoomId_Success() throws Exception {
        // Given
        Long requestId = 100L;
        Set<Long> excludeRooms = Set.of(2L, 3L);
        Long roomId = 1L;
        when(roomService.resolveAvailableRoomId(eq(requestId), any(Set.class))).thenReturn(roomId);

        // When & Then
        mockMvc.perform(get("/api/v1/rooms/available")
                        .header("X-Request-Id", requestId)
                        .param("excludeRooms", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void testIncrementRoomUsage_Success() throws Exception {
        // Given
        Long requestId = 100L;
        Long roomId = 1L;
        doNothing().when(roomService).incrementTimesBooked(requestId, roomId);

        // When & Then
        mockMvc.perform(put("/api/v1/rooms/{roomId}/increment", roomId)
                        .header("X-Request-Id", requestId))
                .andExpect(status().isOk());

        verify(roomService, times(1)).incrementTimesBooked(requestId, roomId);
    }

    @Test
    void testDecrementRoomUsage_Success() throws Exception {
        // Given
        Long requestId = 100L;
        Long roomId = 1L;
        doNothing().when(roomService).decrementTimesBooked(requestId, roomId);

        // When & Then
        mockMvc.perform(put("/api/v1/rooms/{roomId}/decrement", roomId)
                        .header("X-Request-Id", requestId))
                .andExpect(status().isOk());

        verify(roomService, times(1)).decrementTimesBooked(requestId, roomId);
    }

    @Test
    void testCreateRoom_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Given
        CreateRoomPayload invalidPayload = new CreateRoomPayload(null, "");

        // When & Then
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }
}

