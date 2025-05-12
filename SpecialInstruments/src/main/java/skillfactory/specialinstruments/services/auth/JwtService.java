package skillfactory.specialinstruments.services.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static io.jsonwebtoken.SignatureAlgorithm.HS512;

@Slf4j
@Service
public class JwtService {

    @Value("${security.secretKey}")
    private String secretKey;

    @Value("${security.accessTokenExpirationMs}")
    private int accessTokenExpirationMs;

    private Set<String> blockedTokens = new HashSet<>();

    private Key signingKey;

    @PostConstruct
    public void initKey() {
        if (signingKey == null) {
            byte[] apiKeySecretBytes = Base64.encode(secretKey.getBytes());
            this.signingKey = new SecretKeySpec(apiKeySecretBytes, HS512.getJcaName());
        }
    }

    public String generateTokenFromEmail(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromEmail(userPrincipal.getUsername().toLowerCase());
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken, HttpServletRequest request) {
        if (blockedTokens.contains(authToken)) {
            log.error("Token is blocked: {} ", authToken);
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception during parsing token", e);
        }

        return false;
    }

    public void blockToken(String token) {
        blockedTokens.add(token);
    }
}
