package org.example.finaljava.dto.notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record SuccessNotification(String mess) implements Notification{
    @Override
    public void print() {
        log.info("Success! {}", mess);
    }
}
