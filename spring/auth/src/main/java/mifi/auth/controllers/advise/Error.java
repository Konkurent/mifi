package mifi.auth.controllers.advise;


import com.fasterxml.jackson.annotation.JsonGetter;

import java.io.Serializable;
import java.util.Date;


public record Error (
        int statusCode,
        int systemCode,
        Date timestamp,
        String message,
        String path,
        Serializable param
) {

    @JsonGetter
    public String system() {
        return "auth";
    }

    public Error(SystemCode systemCode,
                 String message,
                 String path) {
        this(systemCode.getStatus().value(), systemCode.getCode(), new Date(), message, path, null);
    }

    public Error(SystemCode systemCode,
                 String message,
                 String path,
                 Serializable param) {
        this(systemCode.getStatus().value(), systemCode.getCode(), new Date(), message, path, param);
    }
}
