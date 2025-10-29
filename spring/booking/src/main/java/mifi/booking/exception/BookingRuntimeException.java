package mifi.booking.exception;

import lombok.Getter;
import mifi.booking.controllers.advise.ConvertableException;
import mifi.booking.controllers.advise.SystemCode;

@Getter
public class BookingRuntimeException extends RuntimeException implements ConvertableException {

    private final SystemCode systemCode;

    public BookingRuntimeException(String message, SystemCode systemCode) {
        super(message);
        this.systemCode = systemCode;
    }
}
