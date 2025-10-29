package mifi.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mifi.booking.constant.BookingStatus;
import mifi.booking.controllers.advise.SystemCode;
import mifi.booking.converters.BookingDtoConverter;
import mifi.booking.dto.Booking;
import mifi.booking.dto.CreateBookingPayload;
import mifi.booking.entites.BookingEntity;
import mifi.booking.exception.BookingRuntimeException;
import mifi.booking.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateBookingPayload validPayload;
    private BookingEntity testBookingEntity;
    private Booking testBookingDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        validPayload = new CreateBookingPayload(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                false
        );

        testBookingEntity = BookingEntity.builder()
                .id(1L)
                .roomId(1L)
                .startDate(validPayload.startDate())
                .endDate(validPayload.endDate())
                .status(BookingStatus.CONFIRMED)
                .rqId(100L)
                .build();

        testBookingDto = BookingDtoConverter.toDto(testBookingEntity);
    }

    @Test
    void testCreateBooking_Success() throws Exception {
        // Given
        when(bookingService.bookRoom(any(CreateBookingPayload.class))).thenReturn(testBookingEntity);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testGetBookingById_Success() throws Exception {
        // Given
        Long bookingId = 1L;
        when(bookingService.getById(bookingId)).thenReturn(testBookingEntity);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetBookingById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        Long bookingId = 999L;
        when(bookingService.getById(bookingId))
                .thenThrow(new BookingRuntimeException("Not found", SystemCode.NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testFindAllByFilter_Success() throws Exception {
        // Given
        List<BookingEntity> bookings = List.of(testBookingEntity);
        Page<BookingEntity> page = new PageImpl<>(bookings, PageRequest.of(0, 10), 1);
        when(bookingService.getPageByFilter(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void testCancelBooking_Success() throws Exception {
        // Given
        Long bookingId = 1L;
        doNothing().when(bookingService).cancelBooking(bookingId);

        // When & Then
        mockMvc.perform(delete("/api/v1/bookings/{id}", bookingId))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateBooking_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Given
        CreateBookingPayload invalidPayload = new CreateBookingPayload(
                null,
                null,
                null,
                null,
                false
        );

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }
}

