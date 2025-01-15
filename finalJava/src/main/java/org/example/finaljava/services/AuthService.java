package org.example.finaljava.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.finaljava.dao.UserRepository;
import org.example.finaljava.dto.user.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void createUser(String login, String password) {
        String uuid = UUID.randomUUID().toString();
        userRepository.save(
                User.builder()
                        .uuid(uuid)
                        .login(login)
                        .password(password)
                .build()
        );
        log.info("Created user: {}", uuid);
    }

    public void login(String login, String password) {
        User user = userRepository.getByLoginAndPassword(login, password);
        if (user == null) {
            log.error("User not found");
        } else {
            log.info("User logged in: {}", user.uuid());
        }
    }

}
