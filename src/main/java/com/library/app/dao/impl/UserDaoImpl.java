package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.UserDao;
import com.library.app.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Implementation of {@link UserDao} interface for accessing and manipulating user-related data.
 * Uses JDBC and a singleton {@link ConnectionPool} to manage database connections.
 */
@Repository
public class UserDaoImpl implements UserDao {
    private static final String ID_COLUMN = "id";
    private static final String USERNAME_COLUMN = "username";
    private static final String EMAIL_COLUMN = "email";
    private static final String PASSWORD_COLUMN = "password";
    private static final String STATUS_COLUMN = "status";
    private static final String ROLE_NAME_COLUMN = "role_name";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String ORDER_STATUS_COLUMN = "order_status";
    private static final String ORDER_TYPE_COLUMN = "order_type";
    private static final String TITLE_COLUMN = "title";
    private static final String AUTHOR_FIRST_NAME_COLUMN = "author_first_name";
    private static final String AUTHOR_LAST_NAME_COLUMN = "author_last_name";
    private static final String INVENTORY_NUMBER_COLUMN = "inventory_number";
    private static final String FAILED_TO_SAVE_USER_WITH_USERNAME_EMAIL = "Failed to save user with username={}, email={}";
    private static final String DATABASE_ERROR_WHILE_SAVING_USER = "Database error while saving user";
    private static final String ERROR_FINDING_USER_BY_USERNAME = "Error finding user by username={}";
    private static final String DATABASE_ERROR_WHILE_FINDING_USER = "Database error while finding user";
    private static final String FAILED_TO_LOAD_USER_DATA_FOR_USER_ID = "Failed to load user data for userId={}";
    private static final String ERROR_LOADING_USER_DATA = "Error loading user data";
    private static final String FAILED_TO_LOAD_READERS_WITH_ACTIVE_ORDERS = "Failed to load readers with active orders";
    private static final String DATABASE_ERROR_WHILE_FINDING_ACTIVE_ORDERS = "Database error while finding active orders";
    private static final String FAILED_TO_COUNT_USERS_BY_STATUS = "Failed to count users by status {}";
    private static final String DATABASE_ERROR_WHILE_COUNTING_USER_BY_STATUS = "Database error while counting user by status";
    private static final String ERROR_FETCHING_USERS = "Error fetching users";
    private static final String DATABASE_ERROR_WHILE_FINDING_ALL_USERS_WITH_ROLE =
            "Database error while finding all users with role";
    private static final String ERROR_UPDATING_USER_WITH_ID = "Error updating user with id={}";
    private static final String DATABASE_ERROR_WHILE_UPDATING_USER = "Database error while updating user";
    private static final String ERROR_DELETING_USER_WITH_ID = "Error deleting user with id {}";
    private static final String DATABASE_ERROR_WHILE_DELETING_USER = "Database error while deleting user";
    private static final String ERROR_FINDING_USER_BY_ID = "Error finding user by ID: {}";
    private static final String DATABASE_ERROR_WHILE_FINDING_USER_BY_ID = "Database error while finding user by id";
    private static final String ROLE_NOT_FOUND = "Role not found: {}";
    private static final String ROLE_NOT_FOUND_EXCEPTION = "Role not found %s";
    private static final String ERROR_RETRIEVING_ROLE_ID_FOR_ROLE = "Error retrieving role ID for role={}";
    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";
    private static final String COUNT_USERS_BY_STATUS = "SELECT COUNT(*) FROM users WHERE status = ?";
    private static final String SELECT_ROLE_ID_BY_NAME = "SELECT id FROM roles WHERE name = ?";
    private static final String INSERT_NEW_USER =
            "INSERT INTO users (username, email, password, status, role_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_USER_BY_ID =
            "UPDATE users SET email = ?, password = ?, status = ?, role_id = ? WHERE id = ?";
    private static final String SELECT_USER_WITH_ROLE_BY_USERNAME = """
            SELECT u.*, r.name AS role_name
            FROM users u
            JOIN roles r
            ON u.role_id = r.id
            WHERE u.username = ?""";
    private static final String SELECT_ACTIVE_USER_ORDERS_WITH_BOOK_DETAILS = """
                SELECT u.id AS user_id, u.username, u.email,
                       o.id AS order_id, o.order_status, o.order_type,
                       b.title, b.author_first_name, b.author_last_name,
                       bc.inventory_number
                FROM users u
                JOIN orders o ON u.id = o.user_id
                JOIN book_copies bc ON o.copy_id = bc.id
                JOIN books b ON bc.book_id = b.id
                WHERE o.order_status IN ('PENDING', 'ISSUED')
                ORDER BY u.username, o.issue_date DESC
            """;
    private static final String SELECT_ALL_USERS_WITH_ROLE_NAMES = """
            SELECT u.*, r.name AS role_name
            FROM users u
            JOIN roles r
            ON u.role_id = r.id
            ORDER BY id""";
    private static final String SELECT_USER_WITH_ROLE_BY_ID = """
                SELECT u.*, r.name AS role_name
                FROM users u
                JOIN roles r
                ON u.role_id = r.id
                WHERE u.id = ?
            """;
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();

    /**
     * Persists a new user to the database.
     *
     * @param user the {@link User} entity to be saved
     */
    @Override
    public void save(User user) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            int roleId = getRoleId(user.getRole(), connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_USER)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());
                preparedStatement.setString(3, user.getPassword());
                preparedStatement.setString(4, user.getStatus());
                preparedStatement.setInt(5, roleId);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_SAVE_USER_WITH_USERNAME_EMAIL,
                    user.getUsername(), user.getEmail(), e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_SAVING_USER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username
     * @return an {@link Optional} containing the found user or empty if not found
     */
    @Override
    public Optional<User> findByUsername(String username) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_WITH_ROLE_BY_USERNAME)) {
                preparedStatement.setString(1, username);

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getLong(ID_COLUMN));
                        user.setUsername(rs.getString(USERNAME_COLUMN));
                        user.setEmail(rs.getString(EMAIL_COLUMN));
                        user.setPassword(rs.getString(PASSWORD_COLUMN));
                        user.setStatus(rs.getString(STATUS_COLUMN));
                        user.setRole(Role.valueOf(rs.getString(ROLE_NAME_COLUMN)));

                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_FINDING_USER_BY_USERNAME, username, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_USER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Returns a map of users and their corresponding active orders (PENDING or ISSUED).
     *
     * @return a {@link Map} where each key is a {@link User} and the value is a list of {@link Order}
     */
    @Override
    public Map<User, List<Order>> findReadersWithActiveOrders() {
        Connection connection = null;
        Map<User, List<Order>> userOrdersMap = new LinkedHashMap<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SELECT_ACTIVE_USER_ORDERS_WITH_BOOK_DETAILS);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Long userId = resultSet.getLong(USER_ID_COLUMN);
                    User user = userOrdersMap.keySet().stream()
                            .filter(u -> u.getId().equals(userId))
                            .findFirst()
                            .orElseGet(() -> {
                                User newUser = new User();
                                newUser.setId(userId);

                                try {
                                    newUser.setUsername(resultSet.getString(USERNAME_COLUMN));
                                    newUser.setEmail(resultSet.getString(EMAIL_COLUMN));
                                } catch (SQLException e) {
                                    logger.error(FAILED_TO_LOAD_USER_DATA_FOR_USER_ID, userId, e);
                                    throw new RuntimeException(ERROR_LOADING_USER_DATA, e);
                                }
                                userOrdersMap.put(newUser, new ArrayList<>());

                                return newUser;
                            });

                    Order order = new Order();
                    order.setId(resultSet.getLong(ORDER_ID_COLUMN));
                    order.setStatus(OrderStatus.valueOf(resultSet.getString(ORDER_STATUS_COLUMN)));
                    order.setType(OrderType.valueOf(resultSet.getString(ORDER_TYPE_COLUMN)));

                    Book book = new Book();
                    book.setTitle(resultSet.getString(TITLE_COLUMN));
                    book.setAuthorFirstName(resultSet.getString(AUTHOR_FIRST_NAME_COLUMN));
                    book.setAuthorLastName(resultSet.getString(AUTHOR_LAST_NAME_COLUMN));

                    BookCopy copy = new BookCopy();
                    copy.setInventoryNumber(resultSet.getString(INVENTORY_NUMBER_COLUMN));
                    copy.setBook(book);
                    order.setBookCopy(copy);
                    order.setUser(user);

                    userOrdersMap.get(user).add(order);
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_LOAD_READERS_WITH_ACTIVE_ORDERS, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_ACTIVE_ORDERS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return userOrdersMap;
    }

    /**
     * Counts users by their status (e.g., ACTIVE, BLOCKED).
     *
     * @param status the user status
     * @return the number of users with the specified status
     */
    @Override
    public long countUserByStatus(String status) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(COUNT_USERS_BY_STATUS)) {
                preparedStatement.setString(1, status);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }

                    return 0;
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_COUNT_USERS_BY_STATUS, status, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_COUNTING_USER_BY_STATUS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a {@link List} of {@link User} objects
     */
    @Override
    public List<User> findAll() {
        Connection connection = null;
        List<User> users = new ArrayList<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS_WITH_ROLE_NAMES);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong(ID_COLUMN));
                    user.setUsername(resultSet.getString(USERNAME_COLUMN));
                    user.setEmail(resultSet.getString(EMAIL_COLUMN));
                    user.setPassword(resultSet.getString(PASSWORD_COLUMN));
                    user.setStatus(resultSet.getString(STATUS_COLUMN));
                    user.setRole(Role.valueOf(resultSet.getString(ROLE_NAME_COLUMN)));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_FETCHING_USERS, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_ALL_USERS_WITH_ROLE, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return users;
    }

    /**
     * Updates an existing user's data (email, password, status, role).
     *
     * @param user the {@link User} entity with updated information
     */
    @Override
    public void update(User user) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            int roleId = getRoleId(user.getRole(), connection);

            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER_BY_ID)) {
                preparedStatement.setString(1, user.getEmail());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getStatus());
                preparedStatement.setInt(4, roleId);
                preparedStatement.setLong(5, user.getId());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(ERROR_UPDATING_USER_WITH_ID, user.getId(), e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_UPDATING_USER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID
     */
    @Override
    public void delete(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_USER_BY_ID)) {
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(ERROR_DELETING_USER_WITH_ID, id, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_DELETING_USER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the user ID
     * @return an {@link Optional} with the found user or empty if not found
     */
    @Override
    public Optional<User> findById(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_WITH_ROLE_BY_ID)) {
                preparedStatement.setLong(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        User user = new User();
                        user.setId(resultSet.getLong(ID_COLUMN));
                        user.setUsername(resultSet.getString(USERNAME_COLUMN));
                        user.setEmail(resultSet.getString(EMAIL_COLUMN));
                        user.setPassword(resultSet.getString(PASSWORD_COLUMN));
                        user.setStatus(resultSet.getString(STATUS_COLUMN));
                        user.setRole(Role.valueOf(resultSet.getString(ROLE_NAME_COLUMN)));

                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_FINDING_USER_BY_ID, id, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_USER_BY_ID, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Retrieves the role ID from the `roles` table for the given role enum.
     *
     * @param role       the {@link Role} enum value
     * @param connection the active database connection
     * @return the ID of the role
     * @throws SQLException if role not found or a database error occurs
     */
    private int getRoleId(Role role, Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ROLE_ID_BY_NAME)) {
            preparedStatement.setString(1, role.name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(ID_COLUMN);
                } else {
                    logger.error(ROLE_NOT_FOUND, role);
                    throw new SQLException(String.format(ROLE_NOT_FOUND_EXCEPTION, role));
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_RETRIEVING_ROLE_ID_FOR_ROLE, role, e);
            throw e;
        } finally {
            connectionPool.closeConnection(connection);
        }
    }
}