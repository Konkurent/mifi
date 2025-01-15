package org.example.finaljava.dto.link;

import lombok.Builder;
import lombok.Data;
import org.example.finaljava.errors.ExpirationException;
import org.example.finaljava.errors.UnderLimitException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LinkImpl implements Link {

    private final String shortLink;
    private final String fullLink;
    private LocalDateTime expirationDateTime;
    private Long limit;
    private Long numOfJumps = 1L;

    @Builder
    LinkImpl(String fullLink, LocalDateTime expirationDateTime, Long limit) {
        this.fullLink = fullLink;
        this.shortLink = "clck.ru/" + UUID.randomUUID().toString().substring(0, 8);
        this.expirationDateTime = expirationDateTime;
        this.limit = limit;
    }

    public URI getUri() throws ExpirationException, UnderLimitException {
        if (isExpired()) {
            throw new ExpirationException("Error! Exhausted link!");
        }
        if (isExhausted()) {
            throw new UnderLimitException("Error! The transition quota has been exhausted!");
        }
        this.numOfJumps++;
        return URI.create(fullLink);
    }

    @Override
    public boolean isExpired() {
        return expirationDateTime.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isValid() {
        return !isExpired() && !isExhausted();
    }

    @Override
    public boolean isExhausted() {
        return limit != null && numOfJumps > limit;
    }


}
