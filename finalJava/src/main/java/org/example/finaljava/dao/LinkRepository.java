package org.example.finaljava.dao;


import org.example.finaljava.dto.link.Link;
import org.example.finaljava.dto.user.User;
import org.springframework.lang.Nullable;

import java.util.Collection;

public interface LinkRepository {
    @Nullable Link getByUserAndShortLink(User user, String shortLink);
    Collection<Link> getAllNotificationsByUserUUID(User user);
    Link save(User user, Link link);
    void delete(User user, String shortLink);
}
