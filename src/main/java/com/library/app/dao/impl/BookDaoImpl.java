package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.BookDao;
import com.library.app.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link BookDao} for performing CRUD operations on {@code books} table.
 * Uses JDBC and a singleton {@link ConnectionPool} to manage database connections.
 * Supports operations like saving, updating, deleting books, and searching with filters.
 */
@Repository
public class BookDaoImpl implements BookDao {
    private static final String ID_COLUMN = "id";
    private static final String TITLE_COLUMN = "title";
    private static final String AUTHOR_FIRST_NAME_COLUMN = "author_first_name";
    private static final String AUTHOR_LAST_NAME_COLUMN = "author_last_name";
    private static final String GENRE_COLUMN = "genre";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String COVER_URL_COLUMN = "cover_url";
    private static final String ERROR_WHILE_FINDING_BOOK_BY_ID = "Error while finding book by id={}";
    private static final String ERROR_SAVING_BOOK = "Error saving book";
    private static final String ERROR_SEARCH_TITLE_AUTHOR_GENRE =
            "Error while searching books - title: {}, author: {}, genre: {}";
    private static final String FAILED_TO_UPDATE_BOOK_WITH_ID = "Failed to update book with ID={}";
    private static final String FAILED_TO_DELETE_BOOK_WITH_ID = "Failed to delete book with id={}";
    private static final String FAILED_TO_COUNT_BOOKS = "Failed to count books";
    private static final String ORDER_BY_ID = " ORDER BY id";
    private static final String FILTER_BY_TITLE = " AND LOWER(title) LIKE ?";
    private static final String FILTER_BY_GENRE = " AND LOWER(genre) LIKE ?";
    private static final String SQL_WILDCARD = "%";
    private static final String SELECT_ALL_BOOKS_WITH_OPTIONAL_FILTERS = "SELECT * FROM books WHERE 1=1";
    private static final String SELECT_BOOK_BY_ID = "SELECT * FROM books WHERE id = ?";
    private static final String DELETE_BOOK_BY_ID = "DELETE FROM books WHERE id = ?";
    private static final String COUNT_ALL_BOOKS = "SELECT COUNT(*) FROM books";
    private static final String FILTER_BY_AUTHOR =
            " AND (LOWER(author_first_name) LIKE ? OR LOWER(author_last_name) LIKE ?)";
    private static final String INSERT_NEW_BOOK = """
            INSERT INTO books (title, author_first_name, author_last_name, genre, description, cover_url)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_BOOK_BY_ID = """
                UPDATE books
                SET title = ?, author_first_name = ?, author_last_name = ?, genre = ?, description = ?, cover_url = ?
                WHERE id = ?
            """;
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();

    /**
     * Searches for books by optional filters: title, author name, and genre.
     *
     * @param title  the title to search for (case-insensitive)
     * @param author the author to search for (case-insensitive)
     * @param genre  the genre to search for (case-insensitive)
     * @return a list of books matching the search criteria
     */
    @Override
    public List<Book> search(String title, String author, String genre) {
        Connection connection = null;
        List<Book> books = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder querySearch = new StringBuilder(SELECT_ALL_BOOKS_WITH_OPTIONAL_FILTERS);

        try {
            connection = ConnectionPool.getInstance().getConnection();

            if (title != null && !title.isBlank()) {
                querySearch.append(FILTER_BY_TITLE);
                params.add(SQL_WILDCARD + title.toLowerCase() + SQL_WILDCARD);
            }
            if (author != null && !author.isBlank()) {
                querySearch.append(FILTER_BY_AUTHOR);
                params.add(SQL_WILDCARD + author.toLowerCase() + SQL_WILDCARD);
                params.add(SQL_WILDCARD + author.toLowerCase() + SQL_WILDCARD);
            }
            if (genre != null && !genre.isBlank()) {
                querySearch.append(FILTER_BY_GENRE);
                params.add(SQL_WILDCARD + genre.toLowerCase() + SQL_WILDCARD);
            }
            querySearch.append(ORDER_BY_ID);

            try (PreparedStatement preparedStatement = connection.prepareStatement(querySearch.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        books.add(new Book(
                                resultSet.getLong(ID_COLUMN),
                                resultSet.getString(TITLE_COLUMN),
                                resultSet.getString(AUTHOR_FIRST_NAME_COLUMN),
                                resultSet.getString(AUTHOR_LAST_NAME_COLUMN),
                                resultSet.getString(GENRE_COLUMN),
                                resultSet.getString(DESCRIPTION_COLUMN),
                                resultSet.getString(COVER_URL_COLUMN)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_SEARCH_TITLE_AUTHOR_GENRE,
                    title, author, genre, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return books;
    }

    /**
     * Retrieves a book by its unique identifier.
     *
     * @param id the book ID
     * @return an {@link Optional} containing the found book, or empty if not found
     */
    @Override
    public Optional<Book> findById(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BOOK_BY_ID)) {
                preparedStatement.setLong(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery();) {
                    if (resultSet.next()) {
                        Book book = new Book(
                                resultSet.getLong(ID_COLUMN),
                                resultSet.getString(TITLE_COLUMN),
                                resultSet.getString(AUTHOR_FIRST_NAME_COLUMN),
                                resultSet.getString(AUTHOR_LAST_NAME_COLUMN),
                                resultSet.getString(GENRE_COLUMN),
                                resultSet.getString(DESCRIPTION_COLUMN),
                                resultSet.getString(COVER_URL_COLUMN)
                        );

                        return Optional.of(book);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_WHILE_FINDING_BOOK_BY_ID, id, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Persists a new book in the database.
     *
     * @param book the book to save
     */
    @Override
    public void save(Book book) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NEW_BOOK)) {
                preparedStatement.setString(1, book.getTitle());
                preparedStatement.setString(2, book.getAuthorFirstName());
                preparedStatement.setString(3, book.getAuthorLastName());
                preparedStatement.setString(4, book.getGenre());
                preparedStatement.setString(5, book.getDescription());
                preparedStatement.setString(6, book.getCoverUrl());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(ERROR_SAVING_BOOK, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Updates an existing book's data in the database.
     *
     * @param book the book to update
     */
    @Override
    public void update(Book book) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BOOK_BY_ID)) {
                preparedStatement.setString(1, book.getTitle());
                preparedStatement.setString(2, book.getAuthorFirstName());
                preparedStatement.setString(3, book.getAuthorLastName());
                preparedStatement.setString(4, book.getGenre());
                preparedStatement.setString(5, book.getDescription());
                preparedStatement.setString(6, book.getCoverUrl());
                preparedStatement.setLong(7, book.getId());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_UPDATE_BOOK_WITH_ID, book.getId(), e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Deletes a book from the database by its ID.
     *
     * @param id the book ID
     */
    @Override
    public void delete(Long id) {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BOOK_BY_ID)) {
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_DELETE_BOOK_WITH_ID, id, e);
        } finally {
            connectionPool.closeConnection(connection);
        }
    }

    /**
     * Counts the total number of books in the database.
     *
     * @return the number of books in the {@code books} table
     */
    @Override
    public long count() {
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(COUNT_ALL_BOOKS);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            logger.error(FAILED_TO_COUNT_BOOKS, e);
        } finally {
            connectionPool.closeConnection(connection);
        }

        return 0;
    }
}
