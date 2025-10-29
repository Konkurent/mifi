package mifi.hotel.services;

import mifi.hotel.dto.CustomerDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "auth-service", path = "/api/v1/access")
public interface AuthService {

    @GetMapping("/customer/{email}")
    CustomerDetailsDto getCustomerDetails(@PathVariable String email);
}

