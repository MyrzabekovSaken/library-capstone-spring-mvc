package com.library.app.mapper;

import com.library.app.dto.UserDto;
import com.library.app.model.User;

/**
 * Utility class for mapping between {@code User} entities and {@code UserDto} objects.
 */
public class UserMapper {
    /**
     * Converts a {@code User} entity into a {@code UserDto}.
     *
     * @param user the user entity to convert
     * @return the corresponding {@code UserDto} object
     */
    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setStatus(user.getStatus());
        userDto.setRole(user.getRole());

        return userDto;
    }

    /**
     * Converts a {@code UserDto} into a {@code User} entity.
     *
     * @param userDto the user DTO to convert
     * @return the corresponding {@code User} entity
     */
    public static User toEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setStatus(userDto.getStatus());
        user.setRole(userDto.getRole());

        return user;
    }
}
