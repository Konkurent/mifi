package skillfactory.specialinstruments.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import skillfactory.specialinstruments.exception.AccountExistException;

import java.util.Date;

@Slf4j
@RestControllerAdvice
public class AuthControllerAdvice {

    @ExceptionHandler(value = AccountExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleUserAlreadyExistsException(AccountExistException ex, HttpServletRequest request) {
        return ErrorMessage.builder()
                .statusCode(500)
                .path(request.getRequestURI())
                .timestamp(new Date())
                .message(ex.getMessage())
                .systemCode(500)
                .build();
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleUserAlreadyExistsException(BadCredentialsException ex, HttpServletRequest request) {
        return ErrorMessage.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(new Date())
                .message(ex.getMessage())
                .systemCode(HttpStatus.UNAUTHORIZED.value())
                .build();
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handlePortalException(Throwable e, HttpServletRequest request) {
        log.error("Error: ", e);
        return ErrorMessage.builder()
                .statusCode(500)
                .path(request.getRequestURI())
                .timestamp(new Date())
                .message(e.getMessage())
                .systemCode(500)
                .path(request.getServletPath())
                .build();
    }
}
