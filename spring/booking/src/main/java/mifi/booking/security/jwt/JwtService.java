package mifi.booking.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mifi.auth.security.dto.CustomerDetails;
import mifi.auth.security.dto.JwtConfigProperties;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfigProperties jwtConfigProperties;
    private final UserDetailsService userDetailsService;

    public static final String HEADER = "Authorization";
    public static final String JWT_TOKEN_HEADER_PARAM = HEADER;
    public static final String HEADER_PREFIX = "Bearer ";

    public String generateToken(CustomerDetails details) {
        return Jwts.builder()
                .setSubject(details.getUsername())
                .addClaims(Map.of("userId", details.getUserId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtConfigProperties.getMainTokenExpirationTime()))
                .signWith(SignatureAlgorithm.HS512, new String(Base64.getEncoder().encode(jwtConfigProperties.getSecret().getBytes(StandardCharsets.UTF_8))))
                .compact();
    }

    public boolean validateToken(final String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtConfigProperties.getSecret()).parseClaimsJws(authToken);
            return true;
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT Token", ex);
            throw new BadCredentialsException("Invalid JWT token: ", ex);
        } catch (ExpiredJwtException expiredEx) {
            log.debug("JWT Token is expired", expiredEx);
            throw expiredEx;
        }
    }

    public String parseSubject(String jwt) {
        return Jwts.parserBuilder().setSigningKey(jwtConfigProperties.getSecret()).build().parseClaimsJwt(jwt).getBody().getSubject();
    }

    public String getTokenFromRequest(final HttpServletRequest request) {
        String header = request.getHeader(JWT_TOKEN_HEADER_PARAM);
        if (org.apache.commons.lang3.StringUtils.isBlank(header)) {
            throw new AuthenticationServiceException("Authorization header cannot be blank!");
        }
        if (header.length() < HEADER_PREFIX.length()) {
            throw new AuthenticationServiceException("Invalid authorization header size.");
        }
        return header.substring(HEADER_PREFIX.length());
    }
}
