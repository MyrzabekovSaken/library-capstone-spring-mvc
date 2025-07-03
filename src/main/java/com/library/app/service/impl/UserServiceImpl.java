package com.library.app.service.impl;

import com.library.app.dao.UserDao;
import com.library.app.dto.UserDto;
import com.library.app.mapper.UserMapper;
import com.library.app.model.Order;
import com.library.app.model.Role;
import com.library.app.model.User;
import com.library.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing users and user authentication.
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String ATTEMPTED_TO_UPDATE_NON_EXISTENT_USER_ID = "Attempted to update non-existent user ID={}";
    private static final String USER_NOT_FOUND_LOG = "User not found: {}";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String ACTIVE = "ACTIVE";
    private static final String SQL_STATE = "23505";
    private static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    private static final String USER_IS_NOT_ACTIVE = "User is not active";

    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructs a {@code UserServiceImpl} with dependencies for user management and authentication.
     *
     * @param userDao         the DAO responsible for managing user data
     * @param passwordEncoder the password encoder used for secure authentication
     */
    @Autowired
    public UserServiceImpl(UserDao userDao, BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads user details by username for Spring Security.
     *
     * @param username the username
     * @return user details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUsername(username);

        if (!user.getStatus().equals(ACTIVE)) {
            throw new UsernameNotFoundException(USER_IS_NOT_ACTIVE);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    /**
     * Registers a new user (role: READER).
     *
     * @param user the user to register
     */
    @Override
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.READER);
        user.setStatus(ACTIVE);

        try {
            userDao.save(user);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLException exception && SQL_STATE.equals(exception.getSQLState())) {
                throw new RuntimeException(USERNAME_ALREADY_EXISTS, exception);
            }
            throw e;
        }
    }

    /**
     * Updates the user’s profile and optionally their password.
     *
     * @param userDto  the updated user data
     * @param password optional new password
     * @throws RuntimeException if the user is not found
     */
    @Override
    public void updateUser(UserDto userDto, String password) {
        Optional<User> userOptional = userDao.findById(userDto.getId());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmail(userDto.getEmail());
            user.setRole(userDto.getRole());
            user.setStatus(userDto.getStatus());

            if (password != null && !password.trim().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(password);
                user.setPassword(hashedPassword);
            } else {
                user.setPassword(user.getPassword());
            }
            userDao.update(user);
        } else {
            logger.warn(ATTEMPTED_TO_UPDATE_NON_EXISTENT_USER_ID, userDto.getId());
            throw new RuntimeException(USER_NOT_FOUND);
        }
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username
     * @return user object
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public User getUsername(String username) {
        return userDao.findByUsername(username).orElseThrow(() -> {
            logger.warn(USER_NOT_FOUND_LOG, username);

            return new UsernameNotFoundException(USER_NOT_FOUND);
        });
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return optional containing user
     */
    @Override
    public Optional<User> getById(Long id) {
        return userDao.findById(id);
    }

    /**
     * Retrieves all readers along with their active orders.
     *
     * @return map of users to orders
     */
    @Override
    public Map<User, List<Order>> getReadersWithActiveOrders() {
        return userDao.findReadersWithActiveOrders();
    }

    /**
     * Counts users by their status.
     *
     * @param status the status (e.g., ACTIVE, BLOCKED)
     * @return count
     */
    @Override
    public long countByStatus(String status) {
        return userDao.countUserByStatus(status);
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the ID of the user to delete
     */
    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);
    }

    /**
     * Retrieves a list of all UserDto objects from the database.
     *
     * @return a list of all user DTOs
     */
    @Override
    public List<UserDto> getAllUserDtos() {
        return userDao.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a UserDto object by its unique identifier.
     *
     * @param id the ID of the user DTO to retrieve
     * @return an Optional containing the UserDto if found, or an empty Optional if not found
     */
    @Override
    public Optional<UserDto> getDtoById(Long id) {
        return userDao.findById(id).map(UserMapper::toDto);
    }

    /**
     * Fetches a User entity by username and maps it to a UserDto.
     *
     * @param username the username of the user to retrieve
     * @return an Optional containing the mapped UserDto if found, otherwise an empty Optional
     */
    @Override
    public Optional<UserDto> getDtoByUsername(String username) {
        return userDao.findByUsername(username).map(UserMapper::toDto);
    }
}
