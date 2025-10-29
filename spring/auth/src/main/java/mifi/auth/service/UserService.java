package mifi.auth.service;

import mifi.auth.dto.SignUpPayload;
import mifi.auth.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "booking-service", path = "/api/v1/user")
public interface UserService {

    @GetMapping("/login/{login}")
    User getUserByLogin(@PathVariable("login") String login);

    @PostMapping
    User createUser(SignUpPayload payload);

}
