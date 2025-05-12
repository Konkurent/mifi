package skillfactory.specialinstruments.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import skillfactory.specialinstruments.dto.security.SignInRequest;
import skillfactory.specialinstruments.dto.security.SignUpRequest;
import skillfactory.specialinstruments.services.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping
    public String login() {
        return "Welcome to Skill Factory";
    }

    @PostMapping("/signIn")
    public AuthService.Token signIn(@RequestBody SignInRequest request) {
        return authService.signIn(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        if (token == null) return;
        authService.logout(token.substring(7));
    }

    @PostMapping("/signUp")
    public AuthService.Token signUp(@RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/signUp/admin")
    public AuthService.Token createAdmin(@RequestBody SignUpRequest request) {
        return authService.createAdmin(request);
    }

}
