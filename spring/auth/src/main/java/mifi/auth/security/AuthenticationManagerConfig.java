package mifi.auth.security;

import mifi.auth.security.providers.LoginAuthProvider;
import mifi.auth.security.providers.TokenAuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationManagerConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            ObjectPostProcessor<Object> objectPostProcessor,
            LoginAuthProvider loginAuthenticationProvider,
            TokenAuthProvider tokenAuthenticationProvider) throws Exception{
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        builder.authenticationProvider(loginAuthenticationProvider);
        builder.authenticationProvider(tokenAuthenticationProvider);
        return builder.build();
    }

}
