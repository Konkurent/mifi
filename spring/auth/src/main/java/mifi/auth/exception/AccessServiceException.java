package mifi.auth.exception;

import lombok.Getter;
import mifi.auth.controllers.advise.ConvertableException;
import mifi.auth.controllers.advise.SystemCode;

@Getter
public class AccessServiceException extends RuntimeException implements ConvertableException {

    private final SystemCode systemCode;


    public AccessServiceException(String mess, SystemCode systemCode) {
        super(mess);
        this.systemCode = systemCode;
    }
}
