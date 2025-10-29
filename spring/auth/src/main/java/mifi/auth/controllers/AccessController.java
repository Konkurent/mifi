package mifi.auth.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mifi.auth.dto.CreateUserEvent;
import mifi.auth.dto.CustomerDetailsDto;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.security.dto.CustomerDetails;
import mifi.auth.service.AccessService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AccessService accessService;

    @PutMapping
    public String updateAccess(@Valid @RequestBody UpdateUserPayload payload) {
        return accessService.updateAccess(payload);
    }

    @PostMapping
    public void createAccess(@Valid @RequestBody CreateUserEvent payload) {
        accessService.createAccess(payload);
    }

    @DeleteMapping("/{email}")
    public void deleteAccess(@PathVariable String email) {
        accessService.deleteAccess(email);
    }

    @GetMapping("/customer/{email}")
    public CustomerDetailsDto getCustomerDetails(@PathVariable String email) {
        CustomerDetails customerDetails = accessService.getCustomerDetailsByEmail(email);
        return new CustomerDetailsDto(
                customerDetails.getEmail(),
                customerDetails.getUserId(),
                customerDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
    }

}
