package mifi.hotel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mifi.hotel.dto.CreateHotelPayload;
import mifi.hotel.dto.HotelDto;
import mifi.hotel.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HotelController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Autowired
    private ObjectMapper objectMapper;

    private HotelDto testHotelDto;
    private CreateHotelPayload validPayload;

    @BeforeEach
    void setUp() {
        testHotelDto = HotelDto.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address")
                .build();

        validPayload = new CreateHotelPayload("New Hotel", "New Address");
    }

    @Test
    void testCreateHotel_Success() throws Exception {
        // Given
        when(hotelService.createHotel(any(CreateHotelPayload.class))).thenReturn(testHotelDto);

        // When & Then
        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void testGetAllHotels_Success() throws Exception {
        // Given
        List<HotelDto> hotels = List.of(testHotelDto);
        when(hotelService.getAllHotels()).thenReturn(hotels);

        // When & Then
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Hotel"));
    }

    @Test
    void testCreateHotel_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Given
        CreateHotelPayload invalidPayload = new CreateHotelPayload("", "");

        // When & Then
        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }
}

