package org.example.finaljava.dao;

import lombok.extern.slf4j.Slf4j;
import org.example.finaljava.dto.user.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserStorage implements UserRepository{
    private final Map<String, User> customers = new HashMap<>();

    @Override
    public void save(User user) {
        this.customers.put(user.uuid(), user);
    }

    @Override
    public User getByUUID(String uuid) {
        User user = customers.getOrDefault(uuid, null);
        if (user == null) {
            log.error("Пользователь с таким UUID не найден");
        }
        return user;
    }

    @Override
    public User getByLoginAndPassword(String login, String password) {
        return customers.values().stream()
                .filter(u -> u.login().equalsIgnoreCase(login.trim())
                        && u.password().equals(password.trim()))
                .findFirst()
                .orElse(null);
    }
}
