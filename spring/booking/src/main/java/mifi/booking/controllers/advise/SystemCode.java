package mifi.booking.controllers.advise;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SystemCode {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_EXIST(HttpStatus.CONFLICT);



    private final HttpStatus status;
    private final int code;

    SystemCode(HttpStatus httpStatus) {
        status = httpStatus;
        code = ordinal();
    }
}
