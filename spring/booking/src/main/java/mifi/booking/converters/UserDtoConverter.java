package mifi.booking.converters;

import lombok.experimental.UtilityClass;
import mifi.booking.dto.CreateUserEvent;
import mifi.booking.dto.UpdateUserEvent;
import mifi.booking.dto.User;
import mifi.booking.entites.UserEntity;

@UtilityClass
public class UserDtoConverter {

    public static User toDto(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .login(entity.getLogin())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .middleName(entity.getMiddleName())
                .lastName(entity.getLastName())
                .build();
    }

    public static CreateUserEvent toCreateEvent(UserEntity entity) {
        if (entity == null) return null;
        return CreateUserEvent.builder()
                .userId(entity.getId())
                .login(entity.getLogin())
                .email(entity.getEmail())
                .creationDate(entity.getCreationDate())
                .build();
    }

    public static UpdateUserEvent toUpdateEvent(UserEntity entity) {
        return UpdateUserEvent.builder()
                .userId(entity.getId())
                .login(entity.getLogin())
                .email(entity.getEmail())
                .updateTime(entity.getUpdateDate())
                .build();
    }
}
