package mifi.hotel.service;

import lombok.RequiredArgsConstructor;
import mifi.hotel.dto.CreateHotelPayload;
import mifi.hotel.dto.HotelDto;
import mifi.hotel.entities.Hotel;
import mifi.hotel.exception.HotelNotFoundException;
import mifi.hotel.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelDto createHotel(CreateHotelPayload payload) {
        Hotel hotel = Hotel.builder()
                .name(payload.name())
                .address(payload.address())
                .build();
        Hotel saved = hotelRepository.save(hotel);
        return toDto(saved);
    }

    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found with id: " + id));
    }

    private HotelDto toDto(Hotel hotel) {
        return HotelDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .build();
    }
}

