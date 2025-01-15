package org.example.finaljava.dto.link;

import org.example.finaljava.errors.ExpirationException;
import org.example.finaljava.errors.UnderLimitException;

import java.net.URI;

public interface Link extends Expirable, Limitable{
    URI getUri() throws UnderLimitException, ExpirationException;
    String getShortLink();
    String getFullLink();
    default boolean isValid() {
        return true;
    }
}
