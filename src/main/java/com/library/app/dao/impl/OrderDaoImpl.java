package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.OrderDao;
import com.library.app.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OrderDao} that provides JDBC-based operations
 * for managing orders in the library system.
 * Supports CRUD operations, queries by username or status, and analytics.
 * Uses JDBC and a singleton {@link ConnectionPool} to manage database connections.
 */
@Repository
public class OrderDaoImpl implements OrderDao {
    private static final String ID_COLUMN = "id";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String USERNAME_COLUMN = "username";
    private static final String BOOK_ID_COLUMN = "book_id";
    private static final String TITLE_COLUMN = "title";
    private static final String RETURN_DATE_COLUMN = "return_date";
    private static final String ORDER_COUNT_COLUMN = "order_count";
    private static final String COPY_ID_COLUMN = "copy_id";
    private static final String ORDER_TYPE_COLUMN = "order_type";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String ORDER_STATUS_COLUMN = "order_status";
    private static final String ISSUE_DATE_COLUMN = "issue_date";
    private static final String DUE_DATE_COLUMN = "due_date";
    private static final String AUTHOR_FIRST_NAME_COLUMN = "author_first_name";
    private static final String AUTHOR_LAST_NAME_COLUMN = "author_last_name";
    private static final String INVENTORY_NUMBER_COLUMN = "inventory_number";
    private static final String GENRE_COLUMN = "genre";
    private static final String REQUEST_COUNT_COLUMN = "request_count";
    private static final String SQL_CLAUSE_CLOSE_PARENTHESIS = ")";
    private static final String SQL_PLACEHOLDER = "?";
    private static final String SQL_COMMA_SEPARATOR = ", ";
    private static final String FAILED_TO_SAVE_ORDER_FOR_USER_ID_COPY_ID =
            "Failed to save order for user_id={}, copy_id={}";
    private static final String DATABASE_ERROR_WHILE_SAVING_ORDER = "Database error while saving order";
    private static final String FAILED_TO_UPDATE_ORDER_WITH_ID = "Failed to update order with id={}";
    private static final String DATABASE_ERROR_WHILE_UPDATING_ORDER = "Database error while updating order";
    private static final String ERROR_RETRIEVING_ORDERS_FOR_USERNAME = "Error retrieving orders for username={}";
    private static final String DATABASE_ERROR_WHILE_FINDING_USERNAME = "Database error while finding username";
    private static final String ERROR_RETRIEVING_ORDER_BY_ID = "Error retrieving order by id={}";
    private static final String DATABASE_ERROR_WHILE_FINDING_ORDER_BY_ID = "Database error while finding order by id";
    private static final String FAILED_TO_LOAD_ALL_ORDERS_FOR_LIBRARIAN = "Failed to load all orders for librarian";
    private static final String DATABASE_ERROR_WHILE_FINDING_ALL_ORDERS = "Database error while finding all orders";
    private static final String ERROR_CHECKING_ACTIVE_ORDER_FOR_BOOK_ID_AND_USER_ID =
            "Error checking active order for bookId={} and userId={}";
    private static final String DATABASE_ERROR_WHILE_CHECKING_STATUS_ORDER = "Database error while checking status order";
    private static final String FAILED_TO_FIND_ISSUED_USER_FOR_COPY_ID = "Failed to find issued user for copyId={}";
    private static final String DATABASE_ERROR_WHILE_FINDING_USER_WITH_STATUS =
            "Database error while finding user with status";
    private static final String FAILED_TO_COUNT_ORDERS_BY_STATUSES = "Failed to count orders by statuses";
    private static final String DATABASE_ERROR_WHILE_COUNTING_ORDER_STATUS = "Database error while counting order status";
    private static final String FAILED_TO_LOAD_TOP_REQUESTED_BOOKS = "Failed to load top requested books";
    private static final String DATABASE_ERROR_WHILE_FINDING_TOP_REQUESTED_BOOKS =
            "Database error while finding top requested books";
    private static final String FAILED_TO_LOAD_TOP_ACTIVE_USERS = "Failed to load top active users";
    private static final String DATABASE_ERROR_WHILE_FINDING_TOP_ACTIVE_USERS =
            "Database error while finding top active users";
    private static final String UPDATE_ORDER_BY_ID =
            "UPDATE orders SET order_status = ?, due_date = ?, return_date = ? WHERE id = ?";
    private static final String COUNT_ORDERS_BY_STATUSES_PREFIX = "SELECT COUNT(*) FROM orders WHERE order_status IN (";
    private static final String INSERT_NEW_ORDER = """
            INSERT INTO orders (user_id, copy_id, order_type, order_status, issue_date, due_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String SELECT_USER_ORDER_HISTORY_WITH_DETAILS = """
                SELECT o.id as order_id, o.order_type, o.order_status, o.issue_date, o.due_date, o.return_date,
                       u.id as user_id, u.username,
                       b.id as book_id, b.title, b.author_first_name, b.author_last_name,
                       bc.id as copy_id, bc.inventory_number
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN book_copies bc ON o.copy_id = bc.id
                JOIN books b ON bc.book_id = b.id
                WHERE LOWER(u.username) = LOWER(?)
                ORDER BY o.issue_date DESC, o.id DESC
            """;
    private static final String SELECT_ORDER_WITH_DETAILS_BY_ID = """
                SELECT o.*,
                       u.id as user_id, u.username,
                       b.id as book_id, b.title, b.author_first_name, b.author_last_name,
                       bc.id as copy_id, bc.inventory_number
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN book_copies bc ON o.copy_id = bc.id
                JOIN books b ON bc.book_id = b.id
                WHERE o.id = ?
            """;
    private static final String SELECT_ALL_ORDERS_WITH_DETAILS = """
                SELECT o.*,
                    u.id AS user_id, u.username,
                    b.id AS book_id, b.title, b.author_first_name, b.author_last_name,
                    bc.id AS copy_id, bc.inventory_number
                FROM orders o
                JOIN users u ON o.user_id = u.id
                JOIN book_copies bc ON o.copy_id = bc.id
                JOIN books b ON bc.book_id = b.id
                ORDER BY o.issue_date DESC, o.id DESC
            """;
    private static final String CHECK_EXISTS_ACTIVE_ORDER_FOR_USER_AND_BOOK = """
                SELECT 1 FROM orders o
                JOIN book_copies bc ON o.copy_id = bc.id
                WHERE o.user_id = ? AND bc.book_id = ?
                AND o.order_status IN ('PENDING', 'ISSUED')
                LIMIT 1
            """;
    private static final String SELECT_USER_WITH_ISSUED_OR_PENDING_ORDER_BY_COPY_ID = """
                SELECT u.username
                FROM orders o
                JOIN users u ON o.user_id = u.id
                WHERE o.copy_id = ? AND (o.order_status = 'ISSUED' OR o.order_status = 'PENDING')
                LIMIT 1
            """;
    private static final String SELECT_MOST_REQUESTED_BOOKS = """
                SELECT b.id, b.title, b.author_first_name, b.author_last_name, b.genre,
                COUNT(*) AS request_count
                FROM orders o
                JOIN book_copies bc ON o.copy_id = bc.id
                JOIN books b ON bc.book_id = b.id
                WHERE o.order_status IN ('ISSUED', 'RETURNED')
                GROUP BY b.id, b.title, b.author_first_name, b.author_last_name, b.genre
                ORDER BY request_count DESC
                LIMIT ?
            """;
    private static final String SELECT_TOP_ACTIVE_USERS_BY_ORDER_COUNT = """
            SELECT u.id, u.username, COUNT(*) AS order_count
            FROM orders o
            JOIN users u ON o.user_id = u.id
            WHERE u.status = 'ACTIVE'
              AND o.order_status IN ('ISSUED', 'RETURNED')
            GROUP BY u.id, u.username
            ORDER BY order_count DESC
            LIMIT ?
            """;
    private static final Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);
    private final ConnectionPool connectionPool = ConnectionPool.getInstance();

    /**
     * Saves a new order to the database.
     *
     * @param order the {@link Order} entity to be saved
     */
    @Override
    public void save(Order order) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_ORDER)) {
                preparedStatement.setLong(1, order.getUser().getId());
                preparedStatement.setLong(2, order.getBookCopy().getId());
                preparedStatement.setString(3, order.getType().name());
                preparedStatement.setString(4, order.getStatus().name());
                preparedStatement.setDate(5, Date.valueOf(order.getIssueDate()));
                preparedStatement.setDate(6, Date.valueOf(order.getDueDate()));
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_SAVE_ORDER_FOR_USER_ID_COPY_ID,
                    order.getUser().getId(), order.getBookCopy().getId(), e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_SAVING_ORDER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Updates an existing order's status, due date, and return date.
     *
     * @param order the {@link Order} entity with updated data
     */
    @Override
    public void update(Order order) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ORDER_BY_ID)) {
                preparedStatement.setString(1, order.getStatus().name());

                if (order.getDueDate() != null) {
                    preparedStatement.setDate(2, Date.valueOf(order.getDueDate()));
                } else {
                    preparedStatement.setNull(2, Types.DATE);
                }

                if (order.getReturnDate() != null) {
                    preparedStatement.setDate(3, Date.valueOf(order.getReturnDate()));
                } else {
                    preparedStatement.setNull(3, Types.DATE);
                }

                preparedStatement.setLong(4, order.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_UPDATE_ORDER_WITH_ID, order.getId(), e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_UPDATING_ORDER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Retrieves all orders placed by a specific user, identified by username.
     *
     * @param username the username
     * @return list of {@link Order} associated with the user
     */
    @Override
    public List<Order> findByUsername(String username) {
        Connection connection = null;
        List<Order> orders = new ArrayList<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SELECT_USER_ORDER_HISTORY_WITH_DETAILS)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        User user = new User();
                        user.setId(resultSet.getLong(USER_ID_COLUMN));
                        user.setUsername(resultSet.getString(USERNAME_COLUMN));

                        Book book = new Book(
                                resultSet.getLong(BOOK_ID_COLUMN),
                                resultSet.getString(TITLE_COLUMN),
                                resultSet.getString(AUTHOR_FIRST_NAME_COLUMN),
                                resultSet.getString(AUTHOR_LAST_NAME_COLUMN),
                                null, null, null
                        );
                        BookCopy copy = new BookCopy(
                                resultSet.getLong(COPY_ID_COLUMN),
                                resultSet.getString(INVENTORY_NUMBER_COLUMN),
                                book,
                                null
                        );
                        Order order = new Order(
                                resultSet.getLong(ORDER_ID_COLUMN), user, copy,
                                OrderType.valueOf(resultSet.getString(ORDER_TYPE_COLUMN)),
                                OrderStatus.valueOf(resultSet.getString(ORDER_STATUS_COLUMN)),
                                resultSet.getDate(ISSUE_DATE_COLUMN) != null
                                        ? resultSet.getDate(ISSUE_DATE_COLUMN).toLocalDate() : null,
                                resultSet.getDate(DUE_DATE_COLUMN) != null
                                        ? resultSet.getDate(DUE_DATE_COLUMN).toLocalDate() : null,
                                resultSet.getDate(RETURN_DATE_COLUMN) != null
                                        ? resultSet.getDate(RETURN_DATE_COLUMN).toLocalDate() : null
                        );

                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_RETRIEVING_ORDERS_FOR_USERNAME, username, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_USERNAME, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return orders;
    }

    /**
     * Finds an order by its unique identifier.
     *
     * @param id the order ID
     * @return an {@link Optional} containing the found order or empty if not found
     */
    @Override
    public Optional<Order> findById(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ORDER_WITH_DETAILS_BY_ID)) {
                preparedStatement.setLong(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        Book book = new Book(
                                resultSet.getLong(BOOK_ID_COLUMN),
                                resultSet.getString(TITLE_COLUMN),
                                resultSet.getString(AUTHOR_FIRST_NAME_COLUMN),
                                resultSet.getString(AUTHOR_LAST_NAME_COLUMN),
                                null, null, null
                        );
                        BookCopy copy = new BookCopy(
                                resultSet.getLong(COPY_ID_COLUMN),
                                resultSet.getString(INVENTORY_NUMBER_COLUMN),
                                book,
                                null
                        );
                        User user = new User();
                        user.setId(resultSet.getLong(USER_ID_COLUMN));
                        user.setUsername(resultSet.getString(USERNAME_COLUMN));
                        Order order = new Order(
                                resultSet.getLong(ID_COLUMN),
                                user,
                                copy,
                                OrderType.valueOf(resultSet.getString(ORDER_TYPE_COLUMN)),
                                OrderStatus.valueOf(resultSet.getString(ORDER_STATUS_COLUMN)),
                                resultSet.getDate(ISSUE_DATE_COLUMN) != null
                                        ? resultSet.getDate(ISSUE_DATE_COLUMN).toLocalDate() : null,
                                resultSet.getDate(DUE_DATE_COLUMN) != null
                                        ? resultSet.getDate(DUE_DATE_COLUMN).toLocalDate() : null,
                                resultSet.getDate(RETURN_DATE_COLUMN) != null
                                        ? resultSet.getDate(RETURN_DATE_COLUMN).toLocalDate() : null
                        );
                        return Optional.of(order);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_RETRIEVING_ORDER_BY_ID, id, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_ORDER_BY_ID, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Retrieves all orders in the system, including user and book details.
     *
     * @return list of all {@link Order} entities
     */
    @Override
    public List<Order> findAllOrders() {
        Connection connection = null;
        List<Order> orders = new ArrayList<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ORDERS_WITH_DETAILS);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    Book book = new Book(
                            resultSet.getLong(BOOK_ID_COLUMN),
                            resultSet.getString(TITLE_COLUMN),
                            resultSet.getString(AUTHOR_FIRST_NAME_COLUMN),
                            resultSet.getString(AUTHOR_LAST_NAME_COLUMN),
                            null, null, null
                    );
                    BookCopy copy = new BookCopy(
                            resultSet.getLong(COPY_ID_COLUMN),
                            resultSet.getString(INVENTORY_NUMBER_COLUMN),
                            book,
                            null
                    );
                    User user = new User();
                    user.setId(resultSet.getLong(USER_ID_COLUMN));
                    user.setUsername(resultSet.getString(USERNAME_COLUMN));

                    Order order = new Order(
                            resultSet.getLong(ID_COLUMN),
                            user,
                            copy,
                            OrderType.valueOf(resultSet.getString(ORDER_TYPE_COLUMN)),
                            OrderStatus.valueOf(resultSet.getString(ORDER_STATUS_COLUMN)),
                            resultSet.getDate(ISSUE_DATE_COLUMN) != null
                                    ? resultSet.getDate(ISSUE_DATE_COLUMN).toLocalDate() : null,
                            resultSet.getDate(DUE_DATE_COLUMN) != null
                                    ? resultSet.getDate(DUE_DATE_COLUMN).toLocalDate() : null,
                            resultSet.getDate(RETURN_DATE_COLUMN) != null
                                    ? resultSet.getDate(RETURN_DATE_COLUMN).toLocalDate() : null
                    );
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_LOAD_ALL_ORDERS_FOR_LIBRARIAN, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_ALL_ORDERS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return orders;
    }

    /**
     * Checks if a user already has an active (PENDING or ISSUED) order
     * for a given book.
     *
     * @param bookId the book ID
     * @param userId the user ID
     * @return true if an active order exists, false otherwise
     */
    @Override
    public boolean hasActiveOrderForBook(Long bookId, Long userId) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(CHECK_EXISTS_ACTIVE_ORDER_FOR_USER_AND_BOOK)) {
                preparedStatement.setLong(1, userId);
                preparedStatement.setLong(2, bookId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_CHECKING_ACTIVE_ORDER_FOR_BOOK_ID_AND_USER_ID, bookId, userId, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_CHECKING_STATUS_ORDER, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Finds the username of the user who currently has a book copy
     * either issued or pending.
     *
     * @param copyId the copy ID
     * @return optional username of the user if found
     */
    @Override
    public Optional<String> findIssuedOrReserved(Long copyId) {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SELECT_USER_WITH_ISSUED_OR_PENDING_ORDER_BY_COPY_ID)) {
                preparedStatement.setLong(1, copyId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getString(USERNAME_COLUMN));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_FIND_ISSUED_USER_FOR_COPY_ID, copyId, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_USER_WITH_STATUS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Counts how many orders have statuses matching the provided list.
     *
     * @param statuses list of order statuses
     * @return the number of orders with matching statuses
     */
    @Override
    public long countOrderStatus(List<OrderStatus> statuses) {
        Connection connection = null;

        if (statuses == null || statuses.isEmpty()) {
            return 0;
        }

        String collected = statuses.stream().map(s -> SQL_PLACEHOLDER).collect(Collectors.joining(SQL_COMMA_SEPARATOR));
        String query = COUNT_ORDERS_BY_STATUSES_PREFIX + collected + SQL_CLAUSE_CLOSE_PARENTHESIS;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < statuses.size(); i++) {
                    preparedStatement.setString(i + 1, statuses.get(i).name());
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }

                    return 0;
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_COUNT_ORDERS_BY_STATUSES, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_COUNTING_ORDER_STATUS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Retrieves the most requested books based on the number of ISSUED or RETURNED orders.
     *
     * @param limit the maximum number of results
     * @return list of object arrays where each row contains:
     * [bookId, title, authorFirstName, authorLastName, genre, requestCount]
     */
    @Override
    public List<Object[]> findTopRequestedBooks(int limit) {
        Connection connection = null;
        List<Object[]> result = new ArrayList<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MOST_REQUESTED_BOOKS)) {
                preparedStatement.setInt(1, limit);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Object[] row = new Object[6];
                        row[0] = resultSet.getLong(ID_COLUMN);
                        row[1] = resultSet.getString(TITLE_COLUMN);
                        row[2] = resultSet.getString(AUTHOR_FIRST_NAME_COLUMN);
                        row[3] = resultSet.getString(AUTHOR_LAST_NAME_COLUMN);
                        row[4] = resultSet.getString(GENRE_COLUMN);
                        row[5] = resultSet.getLong(REQUEST_COUNT_COLUMN);
                        result.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_LOAD_TOP_REQUESTED_BOOKS, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_TOP_REQUESTED_BOOKS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return result;
    }

    /**
     * Retrieves the most active users based on the number of ISSUED or RETURNED orders.
     *
     * @param limit the maximum number of users
     * @return list of object arrays where each row contains:
     * [userId, username, orderCount]
     */
    @Override
    public List<Object[]> findTopActiveUsers(int limit) {
        List<Object[]> result = new ArrayList<>();
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SELECT_TOP_ACTIVE_USERS_BY_ORDER_COUNT)) {
                preparedStatement.setInt(1, limit);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Object[] row = new Object[3];
                        row[0] = resultSet.getLong(ID_COLUMN);
                        row[1] = resultSet.getString(USERNAME_COLUMN);
                        row[2] = resultSet.getLong(ORDER_COUNT_COLUMN);
                        result.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_LOAD_TOP_ACTIVE_USERS, e);
            throw new RuntimeException(DATABASE_ERROR_WHILE_FINDING_TOP_ACTIVE_USERS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return result;
    }
}
