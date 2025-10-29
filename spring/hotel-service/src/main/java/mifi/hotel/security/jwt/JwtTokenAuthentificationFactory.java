package mifi.hotel.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mifi.hotel.dto.CustomerDetailsDto;
import mifi.hotel.security.dto.CustomerDetails;
import mifi.hotel.services.AuthService;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenAuthentificationFactory {

    private final AuthService authService;

    public Authentication stubAuthentication(String token) {
        return new TokenHolder(token);
    }

    public Authentication createAuthentication(String email) {
        CustomerDetailsDto customerDetailsDto = authService.getCustomerDetails(email);
        List<GrantedAuthority> authorities = customerDetailsDto.authorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        CustomerDetails customerDetails = CustomerDetails.builder()
                .email(customerDetailsDto.email())
                .userId(customerDetailsDto.userId())
                .password("")
                .authorities(authorities)
                .build();
        
        return new PrincipalHolder(customerDetails);
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

