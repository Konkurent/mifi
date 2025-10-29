package mifi.booking.controllers.advise;

import java.io.Serializable;

public interface ConvertableException {

    String getMessage();

    SystemCode getSystemCode();

    default Serializable getParam() {
        return null;
    }

}
