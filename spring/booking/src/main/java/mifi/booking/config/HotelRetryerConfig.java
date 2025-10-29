package mifi.booking.config;

import feign.Retryer;
import feign.Request;
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

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, 10000);
    }
}
