package mifi.auth.controllers.advise;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SystemCode {
    NOT_FOUND(HttpStatus.NOT_FOUND)
    ;



    private final HttpStatus status;
    private final int code;

    SystemCode(HttpStatus httpStatus) {
        status = httpStatus;
        code = ordinal();
    }
}
