package mifi.booking.config;

import feign.Retryer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HotelRetryerConfig {
    @Bean
    public Retryer hotelRetryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

}
