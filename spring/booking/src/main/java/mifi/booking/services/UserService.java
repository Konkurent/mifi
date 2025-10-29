package mifi.booking.services;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import mifi.booking.controllers.advise.SystemCode;
import mifi.booking.converters.UserDtoConverter;
import mifi.booking.dto.CreateUserPayload;
import mifi.booking.dto.UpdateUserPayload;
import mifi.booking.entites.UserEntity;
import mifi.booking.exception.UserDuplicateException;
import mifi.booking.exception.UserRuntimeException;
import mifi.booking.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final AccessService accessService;
    private final UserRepository userRepository;

    public UserEntity create(CreateUserPayload payload) {
        assertUniqueUser(payload.email(), payload.login());
        UserEntity entity = UserEntity.builder()
                .email(payload.email())
                .login(payload.login())
                .firstName(payload.firstName())
                .lastName(payload.lastName())
                .middleName(payload.middleName())
                .build();
        userRepository.save(entity);
        accessService.createAccess(UserDtoConverter.toCreateEvent(entity));
        return entity;
    }

    private void assertUniqueUser(String email, String login) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDuplicateException("User email already exists", SystemCode.ALREADY_EXIST);
        }
        if (userRepository.existsByLoginIgnoreCase(login)) {
            throw new UserDuplicateException("User login already exists", SystemCode.ALREADY_EXIST);
        }
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            UserEntity entity = userRepository.getReferenceById(id);
            userRepository.delete(entity);
            accessService.deleteAccess(entity.getEmail());
        }
    }

    public UserEntity getUserByLogin(@Valid @NotBlank String login) {
        return userRepository.getByLoginIgnoreCase(login);
    }

    public UserEntity update(@Valid UpdateUserPayload payload) {
        UserEntity entity = userRepository.findById(payload.userId())
                .orElseThrow(() -> new UserRuntimeException("User not found", SystemCode.NOT_FOUND));
        entity.setFirstName(payload.firstName());
        entity.setLastName(payload.lastName());
        entity.setMiddleName(payload.middleName());
        entity.setEmail(payload.email());
        entity.setLogin(payload.login());
        userRepository.save(entity);
        accessService.updateAccess(UserDtoConverter.toUpdateEvent(entity));
        return entity;
    }
}
