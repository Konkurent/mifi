package org.example.finaljava.dao;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.finaljava.dto.link.Link;
import org.example.finaljava.dto.user.User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkStorage implements LinkRepository {

    private final List<LinkStorageCleanCallback> callbacks;

    private final Map<User, Map<String, Link>> links = new HashMap<>();

    @Override
    public Link getByUserAndShortLink(User user, String shortLink) {
        Link link = Optional.ofNullable(user).map(links::get).map(links -> links.get(shortLink)).orElse(null);
        if (link == null) {
            log.error("Ссылка не было найдена");
        }
        return link;
    }

    @Override
    public Collection<Link> getAllNotificationsByUserUUID(User user) {
        return Optional.ofNullable(links.get(user)).map(Map::values).orElseGet(List::of);
    }

    @Override
    public Link save(User user, Link link) {
        Map<String, Link> linksByUser = Optional.ofNullable(user).map(links::get).orElseGet(HashMap::new);
        linksByUser.put(link.getShortLink(), link);
        links.put(user, linksByUser);
        return link;
    }

    @Override
    public void delete(User user, String shortLink) {
        Optional.ofNullable(links.get(user)).ifPresent(userLinks -> userLinks.remove(shortLink));
    }

    @PostConstruct
    public void init() {
        Thread thread = new LinkStorageCleaner();
        thread.setDaemon(true);
        thread.start();
    }

    private class LinkStorageCleaner extends Thread {
        private final static String EXPIRATION_TEMPLATE = "Ссылка [%s] истекла! Заведите ее заново!";
        private final static String LIMIT_TEMPLATE = "Лимит переходов по ссылке [%s] исчеран";

        @Override
        public void run() {
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(15);
                    cleanStorage();
                }
            } catch (Throwable throwable) {
                log.error(throwable.getMessage(), throwable);
            }
        }

        private void cleanStorage() {
            links.entrySet().forEach(linkMap -> {
                List<Link> invalidLinks = resolveNotValidLinks(linkMap.getValue()).stream().map(linkMap.getValue()::remove).filter(Objects::nonNull).toList();
                invalidLinks.forEach(link -> callbacks.forEach(callback -> callback.linkDeleted(linkMap.getKey(), link)));
            });
        }

        private List<String> resolveNotValidLinks(Map<String, Link> userLinks) {
            return userLinks.entrySet().stream().filter(it -> !it.getValue().isValid()).map(Map.Entry::getKey).toList();
        }

    }

    public interface LinkStorageCleanCallback {
        void linkDeleted(User user, Link link);
    }
}
