package org.example.finaljava.dto.notification;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ErrorNotification(String mess) implements Notification {
    @Override
    public void print() {
        log.error("Error! {}", mess);
    }
}
