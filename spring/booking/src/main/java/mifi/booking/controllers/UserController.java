package mifi.booking.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import mifi.booking.converters.UserDtoConverter;
import mifi.booking.dto.CreateUserPayload;
import mifi.booking.dto.UpdateUserPayload;
import mifi.booking.dto.User;
import mifi.booking.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/login/{login}")
    public User getUserByLogin(@Valid @NotBlank @PathVariable String login) {
        return UserDtoConverter.toDto(userService.getUserByLogin(login));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@Valid @NotNull @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Valid @RequestBody CreateUserPayload payload) {
        return UserDtoConverter.toDto(userService.create(payload));
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Valid @RequestBody UpdateUserPayload payload) {
        return UserDtoConverter.toDto(userService.update(payload));
    }
}

