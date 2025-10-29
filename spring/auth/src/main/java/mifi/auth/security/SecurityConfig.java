package mifi.auth.security;

import lombok.RequiredArgsConstructor;
import mifi.auth.security.dto.JwtConfigProperties;
import mifi.auth.security.dto.Urls;
import mifi.auth.security.filters.LoginFilter;
import mifi.auth.security.filters.TokenFilter;
import mifi.auth.security.jwt.JwtService;
import mifi.auth.security.jwt.JwtTokenAuthentificationFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
public class SecurityConfig {

    private final Urls urls;
    private final JwtService jwtService;
    private final JwtTokenAuthentificationFactory factory;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;


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
                        .anyRequest().authenticated()
                )
                .addFilterBefore(loginFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter filter = new LoginFilter("/*/signIn", authenticationSuccessHandler);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    public TokenFilter tokenProcessingFilter() throws Exception {
        RequestMatcher requestMatcher = new OrRequestMatcher(urls.getPermitAll().stream().map(AntPathRequestMatcher::new).map(RequestMatcher.class::cast).toList());
        TokenFilter filter = new TokenFilter(requestMatcher, jwtService, factory);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

}
