package org.example.finaljava.dao;

import org.example.finaljava.dto.user.User;

public interface UserRepository {

    void save(User user);

    User getByUUID(String uuid);

    User getByLoginAndPassword(String login, String password);
}
