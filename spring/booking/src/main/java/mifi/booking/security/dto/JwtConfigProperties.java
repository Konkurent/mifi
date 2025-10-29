package mifi.booking.security.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = JwtConfigProperties.PREFIX)
public class JwtConfigProperties {

    public final static String PREFIX = "security.jwt";

    private String secret;

    private Long mainTokenExpirationTime;

}
