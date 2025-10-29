package mifi.booking.controllers.advise;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mifi.auth.controllers.advise.Error;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@Slf4j
@RestControllerAdvice
public class AuthControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public mifi.auth.controllers.advise.Error handlePortalException(Throwable e, HttpServletRequest request) {
        log.error("Error: ", e);
        if (e instanceof ConvertableException) {
            return new mifi.auth.controllers.advise.Error(
                    ((ConvertableException) e).getSystemCode(),
                    ((ConvertableException) e).getSystemCode().getStatus().getReasonPhrase(),
                    request.getServletPath(),
                    ((ConvertableException) e).getParam()
            );
        }
        return new Error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Integer.MIN_VALUE,
                new Date(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                request.getServletPath(),
                null
        );
    }
}
