package com.library.app.dao;

import com.library.app.model.Order;
import com.library.app.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO interface for performing operations on users.
 */
public interface UserDao {
    /**
     * Saves a new user to the database.
     *
     * @param user the user to save
     */
    void save(User user);

    /**
     * Updates an existing user.
     *
     * @param user the user to update
     */
    void update(User user);

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     */
    void delete(Long id);

    /**
     * Counts users by their status (e.g., ACTIVE, BLOCKED).
     *
     * @param status the user status
     * @return count of users
     */
    long countUserByStatus(String status);

    /**
     * Finds a user by their username.
     *
     * @param username the username
     * @return optional containing the user
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return optional containing the user
     */
    Optional<User> findById(Long id);

    /**
     * Retrieves all users.
     *
     * @return list of users
     */
    List<User> findAll();

    /**
     * Finds all readers with their active orders.
     *
     * @return map of users to their active orders
     */
    Map<User, List<Order>> findReadersWithActiveOrders();
}
