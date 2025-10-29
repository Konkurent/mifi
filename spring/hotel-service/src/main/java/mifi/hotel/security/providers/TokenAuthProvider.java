package mifi.hotel.security.providers;

import lombok.RequiredArgsConstructor;
import mifi.hotel.security.jwt.JwtService;
import mifi.hotel.security.jwt.JwtTokenAuthentificationFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenAuthProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final JwtTokenAuthentificationFactory jwtTokenAuthentificationFactory;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = ((JwtTokenAuthentificationFactory.TokenHolder) authentication).getToken();
        if (jwtService.validateToken(token)) {
            String email = jwtService.parseSubject(token);
            return jwtTokenAuthentificationFactory.createAuthentication(email);
        }
        throw new BadCredentialsException("Invalid token");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == JwtTokenAuthentificationFactory.PrincipalHolder.class;
    }
}

