package org.example.finaljava.services;

import lombok.RequiredArgsConstructor;
import org.example.finaljava.dao.NotificationRepository;
import org.example.finaljava.dto.notification.Notification;
import org.example.finaljava.dto.notification.SuccessNotification;
import org.example.finaljava.dto.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {


    private final NotificationRepository notificationRepository;


    public void createSuccessNotification(User user, String message) {
        notificationRepository.save(user, new SuccessNotification(message));
    }

    public void printAndRemoveByUser(User user) {
        notificationRepository.getAllByUser(user).forEach(Notification::print);
        notificationRepository.cleanByUser(user);
    }

}
