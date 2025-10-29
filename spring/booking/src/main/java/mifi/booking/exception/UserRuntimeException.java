package mifi.booking.exception;

import lombok.Getter;
import mifi.booking.controllers.advise.ConvertableException;
import mifi.booking.controllers.advise.SystemCode;

@Getter
public class UserRuntimeException extends RuntimeException implements ConvertableException {

    private final SystemCode systemCode;

    public UserRuntimeException(String message, SystemCode systemCode) {
        super(message);
        this.systemCode = systemCode;
    }
}
