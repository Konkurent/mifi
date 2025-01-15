package org.example.finaljava.dao;

import lombok.RequiredArgsConstructor;
import org.example.finaljava.dto.notification.Notification;
import org.example.finaljava.dto.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationStorage implements NotificationRepository{

    private final Map<User, List<Notification>> storage = new HashMap<>();

    @Override
    public void save(User user, Notification notification) {
        List<Notification> notifications = storage.getOrDefault(user, new ArrayList<>());
        notifications.add(notification);
        storage.put(user, notifications);
    }

    @Override
    public List<Notification> getAllByUser(User user) {
        return storage.getOrDefault(user, new ArrayList<>());
    }

    @Override
    public void cleanByUser(User user) {
        storage.remove(user);
    }


}
