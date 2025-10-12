package mifi.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Security {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity builder) throws Exception {
        return builder.authorizeHttpRequests((registry) -> {
                    registry.requestMatchers("/", "/home", "/public").permitAll();
                    registry.anyRequest().authenticated();
                })
                .oauth2Login(Customizer.withDefaults())
                .logout(configurer -> configurer.logoutSuccessUrl("/").permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .build();
    }

}


