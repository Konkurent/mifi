package mifi.client1.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "custom.config")
public class ConfigDTO {

    public String prop1;

    public String prop2;

    public String prop3;

}
