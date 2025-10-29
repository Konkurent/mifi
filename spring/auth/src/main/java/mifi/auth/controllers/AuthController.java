package mifi.auth.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mifi.auth.dto.SignUpPayload;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.service.AccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AccessService accessService;

    @PostMapping("/signUp")
    public String signUp(@Valid @RequestBody SignUpPayload request) {
        return accessService.signUp(request);
    }

}
