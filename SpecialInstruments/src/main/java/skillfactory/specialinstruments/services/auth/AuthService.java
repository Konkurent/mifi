package skillfactory.specialinstruments.services.auth;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import skillfactory.specialinstruments.dto.security.OtpUserDetails;
import skillfactory.specialinstruments.dto.security.SignInRequest;
import skillfactory.specialinstruments.dto.security.SignUpRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AccountService accountService;
    private final AuthenticationProvider authenticationProvider;

    public Token signIn(SignInRequest request) {
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login().trim().toLowerCase(),
                        request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        OtpUserDetails userDetails = (OtpUserDetails) authentication.getPrincipal();
        return Token.builder()
                .token(jwtService.generateJwtToken(authentication))
                .accountId(userDetails.accountId())
                .login(userDetails.login())
                .build();
    }

    public void logout(String accessToken) {
        jwtService.blockToken(accessToken);
    }

    public Token signUp(SignUpRequest request) {
        accountService.createUser(request);
        return signIn(new SignInRequest(request.login(), request.password()));
    }

    public Token createAdmin(SignUpRequest request) {
        accountService.createAdmin(request);
        return signIn(new SignInRequest(request.login(), request.password()));
    }

    @Builder
    public record Token(String token, Long accountId, String login) {}

}
