package mifi.hotel.security;

import lombok.RequiredArgsConstructor;
import mifi.hotel.security.dto.JwtConfigProperties;
import mifi.hotel.security.dto.Urls;
import mifi.hotel.security.filters.TokenFilter;
import mifi.hotel.security.jwt.JwtService;
import mifi.hotel.security.jwt.JwtTokenAuthentificationFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtConfigProperties.class, Urls.class})
@EnableMethodSecurity
public class SecurityConfig {

    private final Urls urls;
    private final JwtService jwtService;
    private final JwtTokenAuthentificationFactory factory;
    private final AuthenticationManager authenticationManager;


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(CorsConfiguration.ALL));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .cors(configurer -> configurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(urls.permitAll.toArray(String[]::new)).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/hotels").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/hotels").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/rooms").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/rooms").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/rooms/recommend").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/rooms/available").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/rooms/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public TokenFilter tokenProcessingFilter() throws Exception {
        RequestMatcher requestMatcher = new OrRequestMatcher(urls.getPermitAll().stream().map(AntPathRequestMatcher::new).map(RequestMatcher.class::cast).toList());
        TokenFilter filter = new TokenFilter(requestMatcher, jwtService, factory);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

}

