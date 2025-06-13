package com.library.app.service;

import com.library.app.dto.UserDto;
import com.library.app.model.Order;
import com.library.app.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing users.
 */
public interface UserService {
    /**
     * Registers a new user.
     *
     * @param user the user to register
     */
    void register(User user);

    /**
     * Updates an existing user, optionally changing password.
     *
     * @param userDto  the updated user data
     * @param password optional new password
     */
    void updateUser(UserDto userDto, String password);

    /**
     * Deletes a user by ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Counts users with the specified status (e.g., ACTIVE, BLOCKED).
     *
     * @param status user status string
     * @return count of users with the given status
     */
    long countByStatus(String status);

    /**
     * Returns user details by username.
     *
     * @param username the username to search for
     * @return found user
     */
    User getUsername(String username);

    /**
     * Returns user by ID.
     *
     * @param id the user ID
     * @return optional containing the user or empty
     */
    Optional<User> getById(Long id);

    /**
     * Returns a map of readers and their currently active orders.
     *
     * @return map with users as keys and list of their active orders as values
     */
    Map<User, List<Order>> getReadersWithActiveOrders();

    /**
     * Retrieves a list of all UserDto objects.
     *
     * @return a list containing all user DTOs
     */
    List<UserDto> getAllUserDtos();

    /**
     * Retrieves a UserDto object by its unique identifier.
     *
     * @param id the ID of the user DTO to retrieve
     * @return an Optional containing the UserDto if found, or an empty Optional if not found
     */
    Optional<UserDto> getDtoById(Long id);

    /**
     * Retrieves an optional UserDto based on the provided username.
     * @param username the username of the user to retrieve
     * @return an Optional containing the UserDto if found, otherwise an empty Optional
     */
    Optional<UserDto> getDtoByUsername(String username);
}
