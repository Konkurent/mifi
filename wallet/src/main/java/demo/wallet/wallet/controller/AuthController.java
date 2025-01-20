package demo.wallet.wallet.controller;

import demo.wallet.wallet.service.AuthService;
import demo.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final WalletService walletService;

    @PutMapping("/login")
    String login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);
    }

    @PutMapping("/logout")
    void logout(@RequestParam String sessionToken) {
        authService.logout(sessionToken);
    }


    @PostMapping("/signUn")
    String signUp(@RequestBody CreateUserPayload payload) {
        String session = authService.createUser(payload);
        walletService.createWallet(session);
        return session;
    }

    public record CreateUserPayload(String login, String pass) {}
}
