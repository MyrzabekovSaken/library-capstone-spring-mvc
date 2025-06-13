package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.BookCopyDao;
import com.library.app.dao.BookDao;
import com.library.app.model.Book;
import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link BookCopyDao} interface for interacting with book copies in the database.
 * Uses JDBC and a singleton {@link ConnectionPool} to manage database connections.
 */
@Repository
public class BookCopyDaoImpl implements BookCopyDao {
    private static final String ID = "id";
    private static final String BOOK_ID = "book_id";
    private static final String BOOK_NOT_FOUND_WITH_ID = "Book not found with ID: %s";
    private static final String STATUS = "status";
    private static final String INVENTORY_NUMBER = "inventory_number";
    private static final String BOOK_NOT_FOUND_WHILE_MAPPING = "Book not found while mapping BookCopy. bookId={}";
    private static final String FAILED_TO_LOAD_BOOK_COPIES_FOR_BOOK_ID = "Failed to load book copies for bookId={}";
    private static final String ERROR_FETCHING_BOOK_COPY_WITH_ID = "Error fetching BookCopy with id {}";
    private static final String ERROR_WHILE_FINDING_AVAILABLE_COPY_FOR_BOOK_ID =
            "Error while finding available copy for bookId={}";
    private static final String SELECT_BOOK_COPY_BY_ID = "SELECT * FROM book_copies WHERE id = ?";
    private static final String COUNT_ALL_BOOK_COPIES = "SELECT COUNT(*) FROM book_copies";
    private static final String FAILED_TO_COUNT_ALL_BOOK_COPIES = "Failed to count all book copies";
    private static final String SELECT_TOTAL_BOOK_COPIES_BY_STATUS = "SELECT COUNT(*) FROM book_copies WHERE status = ?";
    private static final String FAILED_TO_COUNT_BOOK_COPIES_WITH_STATUS = "Failed to count book copies with status {}";
    private static final String ERROR_WHILE_UPDATING_BOOK_COPY_WITH_ID = "Error while updating book copy with id={}";
    private static final String FAILED_TO_SAVE_BOOK_COPY = "Failed to save book copy";
    private static final String FAILED_TO_DELETE_BOOK_COPY = "Failed to delete book copy";
    private static final String DELETE_BOOK_COPY_BY_ID = "DELETE FROM book_copies WHERE id = ?";
    private static final String UPDATE_BOOK_COPY_INV_NUMBER_AND_STATUS_BY_ID =
            "UPDATE book_copies SET inventory_number = ?, status = ? WHERE id = ?";
    private static final String COUNT_AVAILABLE_BOOK_COPIES_BY_BOOK_ID =
            "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE'";
    private static final String ERROR_WHILE_COUNTING_AVAILABLE_COPIES_FOR_BOOK_ID =
            "Error while counting available copies for bookId={}";
    private static final String INSERT_NEW_BOOK_COPY =
            "INSERT INTO book_copies (book_id, inventory_number, status) VALUES (?, ?, ?)";
    private static final String ERROR_FIND_LAST_INV_NUMBER_BOOK_ID =
            "Error while finding last inventory number for bookId={}";
    private static final String SELECT_ONE_AVAILABLE_BOOK_COPY =
            "SELECT * FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' LIMIT 1";
    private static final String SELECT_BOOK_COPIES_ORDERED_BY_INV_NUMBER = """
                SELECT * FROM book_copies
                WHERE book_id = ?
                ORDER BY
                CASE
                WHEN inventory_number ~ '^INV-\\d+$'
                THEN CAST(SUBSTRING(inventory_number FROM 5) AS INTEGER)
                ELSE NULL
                END NULLS LAST
            """;
    private static final String SELECT_LAST_BOOK_COPY_BY_INV_NUMBER = """
                SELECT inventory_number FROM book_copies
                WHERE book_id = ?
                ORDER BY CAST(SUBSTRING(inventory_number FROM 5) AS INTEGER) DESC
                LIMIT 1
            """;
    private static final Logger logger = LoggerFactory.getLogger(BookCopyDaoImpl.class);

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();
    private final BookDao bookDao;

    /**
     * Constructs a new {@code BookCopyDaoImpl} with the specified {@link BookDao}.
     *
     * @param bookDao DAO used to fetch book information
     */
    @Autowired
    public BookCopyDaoImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    /**
     * Finds one available copy of the specified book, if any exists.
     *
     * @param bookId the book ID
     * @return an {@link Optional} containing the available copy, or empty if none found
     */
    @Override
    public Optional<BookCopy> findAvailableCopy(Long bookId) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ONE_AVAILABLE_BOOK_COPY)) {
                preparedStatement.setLong(1, bookId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToBookCopy(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_WHILE_FINDING_AVAILABLE_COPY_FOR_BOOK_ID, bookId, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Retrieves all copies for a specific book by book ID.
     *
     * @param bookId the book ID
     * @return list of {@link BookCopy} instances
     */
    @Override
    public List<BookCopy> findAllByBookId(Long bookId) {
        Connection connection = null;
        List<BookCopy> copies = new ArrayList<>();

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(SELECT_BOOK_COPIES_ORDERED_BY_INV_NUMBER)) {
                preparedStatement.setLong(1, bookId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        copies.add(mapRowToBookCopy(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_LOAD_BOOK_COPIES_FOR_BOOK_ID, bookId, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return copies;
    }

    /**
     * @param id the ID of the copy
     * @return an {@link Optional} with the book copy if found, otherwise empty
     */
    @Override
    public Optional<BookCopy> findById(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BOOK_COPY_BY_ID)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToBookCopy(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_FETCHING_BOOK_COPY_WITH_ID, id, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Retrieves the last used inventory number for a given book.
     *
     * @param bookId the book ID
     * @return an {@link Optional} containing the last inventory number, if available
     */
    @Override
    public Optional<String> findLastInventoryNumber(Long bookId) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LAST_BOOK_COPY_BY_INV_NUMBER)) {
                preparedStatement.setLong(1, bookId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.ofNullable(resultSet.getString(INVENTORY_NUMBER));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_FIND_LAST_INV_NUMBER_BOOK_ID, bookId, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Counts the total number of book copies stored in the system.
     *
     * @return the total number of book copies
     */
    @Override
    public long countAllBookCopy() {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(COUNT_ALL_BOOK_COPIES);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_COUNT_ALL_BOOK_COPIES, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return 0;
    }

    /**
     * Counts the number of book copies with the specified status.
     *
     * @param status the {@link CopyStatus} to filter by
     * @return the number of copies with the given status
     */
    @Override
    public long countBookCopyStatus(CopyStatus status) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOTAL_BOOK_COPIES_BY_STATUS)) {
                preparedStatement.setString(1, status.name());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }

                    return 0;
                }
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_COUNT_BOOK_COPIES_WITH_STATUS, status, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return 0;
    }

    /**
     * Updates the status of an existing book copy in the database.
     *
     * @param copy the book copy with updated status
     */
    @Override
    public void update(BookCopy copy) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(UPDATE_BOOK_COPY_INV_NUMBER_AND_STATUS_BY_ID)) {
                preparedStatement.setString(1, copy.getInventoryNumber());
                preparedStatement.setString(2, copy.getStatus().name());
                preparedStatement.setLong(3, copy.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(ERROR_WHILE_UPDATING_BOOK_COPY_WITH_ID, copy.getId(), e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Returns the number of available book copies for a given book.
     *
     * @param bookId the book ID
     * @return number of copies with status {@code AVAILABLE}
     */
    @Override
    public int countAvailableCopies(Long bookId) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement =
                         connection.prepareStatement(COUNT_AVAILABLE_BOOK_COPIES_BY_BOOK_ID)) {
                preparedStatement.setLong(1, bookId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_WHILE_COUNTING_AVAILABLE_COPIES_FOR_BOOK_ID, bookId, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return 0;
    }

    /**
     * Saves a new book copy to the database.
     *
     * @param copy the {@link BookCopy} to be saved
     */
    @Override
    public void save(BookCopy copy) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_BOOK_COPY)) {
                preparedStatement.setLong(1, copy.getBook().getId());
                preparedStatement.setString(2, copy.getInventoryNumber());
                preparedStatement.setString(3, copy.getStatus().name());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_SAVE_BOOK_COPY, e);
            throw new RuntimeException(FAILED_TO_SAVE_BOOK_COPY, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Deletes a book copy by its ID.
     *
     * @param id the ID of the book copy to delete
     */
    @Override
    public void delete(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BOOK_COPY_BY_ID)) {
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_DELETE_BOOK_COPY, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Maps a {@link ResultSet} row to a {@link BookCopy} object.
     *
     * @param resultSet the JDBC result set
     * @return a mapped {@link BookCopy} object
     * @throws SQLException if reading from the result set fails
     */
    private BookCopy mapRowToBookCopy(ResultSet resultSet) throws SQLException {
        BookCopy copy = new BookCopy();
        copy.setId(resultSet.getLong(ID));
        copy.setInventoryNumber(resultSet.getString(INVENTORY_NUMBER));
        Long bookId = resultSet.getLong(BOOK_ID);
        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> {
                    logger.error(BOOK_NOT_FOUND_WHILE_MAPPING, bookId);
                    return new SQLException(String.format(BOOK_NOT_FOUND_WITH_ID, bookId));
                });
        copy.setBook(book);
        copy.setStatus(CopyStatus.valueOf(resultSet.getString(STATUS)));

        return copy;
    }
}
