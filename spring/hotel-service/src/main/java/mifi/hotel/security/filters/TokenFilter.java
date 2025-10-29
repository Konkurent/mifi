package mifi.hotel.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mifi.hotel.security.jwt.JwtService;
import mifi.hotel.security.jwt.JwtTokenAuthentificationFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public class TokenFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtService jwtService;
    private final JwtTokenAuthentificationFactory jwtTokenAuthentificationFactory;

    public TokenFilter(
            RequestMatcher requiresAuthenticationRequestMatcher,
            JwtService jwtService,
            JwtTokenAuthentificationFactory jwtTokenAuthentificationFactory) {
        super(requiresAuthenticationRequestMatcher);
        this.jwtService = jwtService;
        this.jwtTokenAuthentificationFactory = jwtTokenAuthentificationFactory;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        return getAuthenticationManager().authenticate(jwtTokenAuthentificationFactory.stubAuthentication(jwtService.getTokenFromRequest(request)));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }
}

