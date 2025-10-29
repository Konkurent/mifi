package mifi.hotel.service;

import mifi.hotel.dto.CreateHotelPayload;
import mifi.hotel.dto.HotelDto;
import mifi.hotel.entities.Hotel;
import mifi.hotel.exception.HotelNotFoundException;
import mifi.hotel.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address 123")
                .build();
    }

    @Test
    void testCreateHotel_Success() {
        // Given
        CreateHotelPayload payload = new CreateHotelPayload("New Hotel", "New Address");
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        // When
        HotelDto result = hotelService.createHotel(payload);

        // Then
        assertNotNull(result);
        assertEquals(testHotel.getId(), result.getId());
        assertEquals(testHotel.getName(), result.getName());
        assertEquals(testHotel.getAddress(), result.getAddress());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testGetAllHotels_Success() {
        // Given
        List<Hotel> hotels = List.of(testHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        // When
        List<HotelDto> result = hotelService.getAllHotels();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHotel.getId(), result.get(0).getId());
    }

    @Test
    void testGetHotelById_Success() {
        // Given
        Long hotelId = 1L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(testHotel));

        // When
        Hotel result = hotelService.getHotelById(hotelId);

        // Then
        assertNotNull(result);
        assertEquals(testHotel.getId(), result.getId());
        assertEquals(testHotel.getName(), result.getName());
    }

    @Test
    void testGetHotelById_NotFound_ThrowsException() {
        // Given
        Long hotelId = 999L;
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(HotelNotFoundException.class, () -> {
            hotelService.getHotelById(hotelId);
        });
    }
}

