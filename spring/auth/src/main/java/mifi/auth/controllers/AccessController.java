package mifi.auth.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mifi.auth.dto.CreateUserEvent;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.service.AccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AccessService accessService;

    @PutMapping
    public String updateAccess(@Valid @RequestBody UpdateUserPayload payload) {
        return accessService.updateAccess(payload);
    }

    @PutMapping
    public void createAccess(@Valid @RequestBody CreateUserEvent payload) {
        accessService.createAccess(payload);
    }

    @DeleteMapping("/{email}")
    public void deleteAccess(@PathVariable String email) {
        accessService.deleteAccess(email);
    }

}
