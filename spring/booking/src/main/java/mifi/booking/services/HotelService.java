package mifi.booking.services;

import mifi.booking.config.HotelRequestInterceptorConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "hotel-service",
        path = "/api/v1/hotel",
        configuration = HotelRequestInterceptorConfig.class
)
public interface HotelService {

}
