package mifi.booking.services;

import mifi.booking.dto.CreateUserEvent;
import mifi.booking.dto.CustomerDetailsDto;
import mifi.booking.dto.UpdateUserEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "auth-service", path = "/api/v1/access")
public interface AccessService {

    @PutMapping
    String updateAccess(UpdateUserEvent event);

    @PostMapping
    void createAccess(CreateUserEvent event);

    @DeleteMapping("/{email}")
    void deleteAccess(@PathVariable String email);

    @GetMapping("/customer/{email}")
    CustomerDetailsDto getCustomerDetails(@PathVariable String email);
}
