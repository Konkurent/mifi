package mifi.booking.services;

import lombok.NonNull;
import mifi.booking.config.HotelRetryerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(
        name = "hotel-service",
        path = "/api/v1/rooms",
        configuration = HotelRetryerConfig.class
)
public interface HotelService {

    @GetMapping("/available")
    Long resolveAvailableRoomId(
            @RequestHeader("X-Request-Id") Long rqId,
            @RequestParam(value = "excludeRooms", required = false) Set<Long> excludeRooms);

    @PutMapping("/{roomId}/increment")
    void incrementRoomUsage(
            @RequestHeader("X-Request-Id") Long rqId,
            @PathVariable @NonNull Long roomId);

}
