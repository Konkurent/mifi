package mifi.booking.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import mifi.booking.repository.BookingSequence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HotelRequestInterceptorConfig {

    private final BookingSequence bookingSequence;

    @Bean
    RequestInterceptor hotelRequestInterceptor() {
        return template -> template.header("X-Booking-Id", bookingSequence.next().toString());
    }

}
