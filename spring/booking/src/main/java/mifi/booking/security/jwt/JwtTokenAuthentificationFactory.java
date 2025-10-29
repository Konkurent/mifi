package mifi.booking.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mifi.auth.security.jwt.JwtService;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAuthentificationFactory {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public Authentication stubAuthentication(String token) {
        return new TokenHolder(token);
    }

    public Authentication createAuthentication(String email) {
        return new PrincipalHolder(userDetailsService.loadUserByUsername(email));
    }


    @Getter
    public static class TokenHolder extends AbstractAuthenticationToken {

        private final String token;

        public TokenHolder(String token) {
            super(null);
            this.token = token;
            setAuthenticated(false);
        }


        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }
    }

    public static class PrincipalHolder extends AbstractAuthenticationToken {

        private final UserDetails customerDetails;

        public PrincipalHolder(final UserDetails userDetails) {
            super(userDetails.getAuthorities());
            this.customerDetails = userDetails;
            super.setAuthenticated(true);
            super.eraseCredentials();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public UserDetails getPrincipal() {
            return customerDetails;
        }
    }

}
