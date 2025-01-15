package org.example.finaljava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.finaljava.dao.LinkRepository;
import org.example.finaljava.dao.LinkStorage;
import org.example.finaljava.dao.UserRepository;
import org.example.finaljava.dto.link.Link;
import org.example.finaljava.dto.link.LinkImpl;
import org.example.finaljava.dto.user.User;
import org.example.finaljava.errors.ExpirationException;
import org.example.finaljava.errors.UnderLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    @Value("${link.settings.time.mode}")
    private ChronoUnit defaultMode;

    @Value("${link.settings.time.duration}")
    private Long defaultDuration;

    private final LinkRepository linkRepository;
    private final NotificationService notificationService;
    private final UserRepository repository;

    public void createLink(String uuid, String fullLink, ChronoUnit mode, Long duration, Long limit) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        ChronoUnit resolvedMode = Optional.ofNullable(mode).orElse(defaultMode);
        Long resolvedDuration = Optional.ofNullable(duration).orElse(defaultDuration);
        if (user != null) {
            log.info(
                    "Новая ссылка: {}",
                    linkRepository.save(
                            user,
                            LinkImpl.builder()
                                    .expirationDateTime(LocalDateTime.now().plus(resolvedDuration, resolvedMode))
                                    .fullLink(fullLink)
                                    .limit(limit)
                                    .build()
                    ).getShortLink()
            );
        }
    }

    public void showAllLinks(String uuid) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        if (user != null) {
            linkRepository.getAllNotificationsByUserUUID(user).stream().map(Object::toString).forEach(System.out::println);
        }
    }


    public void updateTimeOut(String uuid, String shortLink, long duration, ChronoUnit mode) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        if (user != null) {
            Optional.ofNullable(linkRepository.getByUserAndShortLink(user, shortLink)).map(LinkImpl.class::cast).ifPresent(link -> {
                link.setExpirationDateTime(link.getExpirationDateTime().plus(duration, defaultMode));
            });
        }
    }

    public void updateLimit(String uuid, String shortLink, long limit) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        if (user != null) {
            Optional.ofNullable(linkRepository.getByUserAndShortLink(user, shortLink)).map(LinkImpl.class::cast).ifPresent(link -> {
                link.setLimit(limit);
            });
        }
    }

    public void deleteLink(String uuid, String shortLink) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        if (user != null) {
            linkRepository.delete(user, shortLink);
            log.info("Ссылка была успешно удалена");
        }
    }

    public void jump(String uuid, String shortLink) {
        User user = repository.getByUUID(uuid);
        notificationService.printAndRemoveByUser(user);
        if (user != null) {
            Optional.ofNullable(linkRepository.getByUserAndShortLink(user, shortLink)).ifPresent(this::jump);
        }
    }


    private void jump(Link link) {
        URI uri = null;
        try {
            uri = link.getUri();
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        URI finalUri = uri;
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(finalUri);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }).run();
    }


    @Component
    @RequiredArgsConstructor
    public static class LinkDeleteNotificationCallback implements LinkStorage.LinkStorageCleanCallback {

        private final static String EXPIRATION_TEMPLATE = "Ссылка [%s] истекла! Заведите ее заново!";
        private final static String LIMIT_TEMPLATE = "Лимит переходов по ссылке [%s] исчеран";

        private final NotificationService notificationService;

        @Override
        public void linkDeleted(User user, Link link) {
            if (link.isExpired()) {
                notificationService.createSuccessNotification(user, "Ссылка [%s: %s] была удалена так как срок ее действия истек!".formatted(link.getShortLink(), link.getFullLink()));
            }
            if (link.isExhausted()) {
                notificationService.createSuccessNotification(user, "Ссылка [%s: %s] была удалена так как лимит был исчерпан!".formatted(link.getShortLink(), link.getFullLink()));
            }
        }
    }
}
