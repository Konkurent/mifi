package org.example.finaljava.dao;

import org.example.finaljava.dto.notification.Notification;
import org.example.finaljava.dto.user.User;

import java.util.List;

public interface NotificationRepository {
    void save(User user, Notification notification);

    List<Notification> getAllByUser(User user);

    void cleanByUser(User user);
}
