package mifi.auth.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mifi.auth.dto.SignInPayload;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.io.IOException;


public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler successHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(
            String defaultFilterProcessesUrl,
            AuthenticationSuccessHandler successHandler) {
        super(defaultFilterProcessesUrl);
        this.successHandler = successHandler;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws HttpRequestMethodNotSupportedException {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            if(logger.isDebugEnabled()) {
                logger.debug("Authentication method not supported. Request method: " + request.getMethod());
            }
            throw new HttpRequestMethodNotSupportedException(request.getMethod());
        }
        SignInPayload payload;
        try {
            payload = objectMapper.readValue(request.getReader(), SignInPayload.class);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid login request payload");
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(payload.userName(), payload.password());
        token.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {
        successHandler.onAuthenticationSuccess(request, response, authResult);;
    }
}
