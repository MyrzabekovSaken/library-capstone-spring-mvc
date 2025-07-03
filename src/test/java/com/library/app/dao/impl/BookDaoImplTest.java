package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookDaoImplTest {
    // 1. Константы
    private static final Long BOOK_ID = 1L;
    private static final String TITLE = "Java";
    private static final String GENRE = "Programming";
    private static final String ID = "id";
    private static final String TITLE_COLUMN = "title";
    private static final String AUTHOR_FIRST_NAME = "author_first_name";
    private static final String AUTHOR_LAST_NAME = "author_last_name";
    private static final String GENRE_COLUMN = "genre";
    private static final String DESCRIPTION = "description";
    private static final String COVER_URL = "cover_url";
    private static final String JOSHUA = "Joshua";
    private static final String BLOCH = "Bloch";
    private static final String EFFECTIVE_JAVA = "Effective Java";
    private static final String URL = "http://img";
    private static final String DB_ERROR = "DB error";
    private static final String TEST = "test";
    private static final String URL_STRING = "url";

    private MockedStatic<ConnectionPool> mockedStatic;

    // 2. Моки
    @Mock
    private ConnectionPool connectionPool;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    // 3. @InjectMocks
    private BookDaoImpl testingInstance;

    @BeforeEach
    void setUp() throws Exception {
        mockedStatic = Mockito.mockStatic(ConnectionPool.class);
        mockedStatic.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        when(connectionPool.getConnection()).thenReturn(connection);
        testingInstance = new BookDaoImpl();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    // 4. Тест
    // POSITIVE TEST

    @Test
    void shouldSearchBook() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        mockResultSetForBook();
        // When
        List<Book> result = testingInstance.search(TITLE, JOSHUA, GENRE);
        // Then
        verify(connection).prepareStatement(anyString());
        verify(preparedStatement).executeQuery();
        assertEquals(1, result.size());
        assertEquals(TITLE, result.get(0).getTitle());
    }

    @Test
    void shouldFindByIdBook() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        mockResultSetForBook();
        // When
        Optional<Book> result = testingInstance.findById(BOOK_ID);
        // Then
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeQuery();
        assertTrue(result.isPresent());
        assertEquals(BOOK_ID, result.get().getId());
    }

    @Test
    void shouldSaveNewBook() throws Exception {
        // Given
        Book book = new Book(BOOK_ID, TITLE, JOSHUA, BLOCH, GENRE, EFFECTIVE_JAVA, URL_STRING);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.save(book);
        // Then
        verify(preparedStatement).setString(1, TITLE);
        verify(preparedStatement).setString(2, JOSHUA);
        verify(preparedStatement).setString(3, BLOCH);
        verify(preparedStatement).setString(4, GENRE);
        verify(preparedStatement).setString(5, EFFECTIVE_JAVA);
        verify(preparedStatement).setString(6, URL_STRING);
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldUpdateBook() throws Exception {
        // Given
        Book book = new Book(BOOK_ID, TITLE, JOSHUA, BLOCH, GENRE, EFFECTIVE_JAVA, URL_STRING);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.update(book);
        // Then
        verify(preparedStatement).setString(1, TITLE);
        verify(preparedStatement).setString(2, JOSHUA);
        verify(preparedStatement).setString(3, BLOCH);
        verify(preparedStatement).setString(4, GENRE);
        verify(preparedStatement).setString(5, EFFECTIVE_JAVA);
        verify(preparedStatement).setString(6, URL_STRING);
        verify(preparedStatement).setLong(7, BOOK_ID);
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldDeleteBook() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.delete(BOOK_ID);
        // Then
        verify(preparedStatement).setLong(1, BOOK_ID);
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldCountBooks() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(5L);
        // When
        long result = testingInstance.count();
        // Then
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeQuery();
        assertEquals(5L, result);
    }

    @Test
    void shouldCountResultEmpty() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        long result = testingInstance.count();
        // Then
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeQuery();
        assertEquals(0L, result);
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotSearchWhenExceptionOccurs() throws Exception {
        // Given
        when(connectionPool.getConnection()).thenThrow(new SQLException(DB_ERROR));
        // When
        List<Book> result = testingInstance.search(TEST, TEST, TEST);
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFindByIdWhenNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        Optional<Book> result = testingInstance.findById(BOOK_ID);
        // Then
        verify(connection).prepareStatement(any());
        verify(preparedStatement).executeQuery();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFoundWhenException() throws Exception {
        // Given
        when(connectionPool.getConnection()).thenThrow(new SQLException());
        // When
        long result = testingInstance.count();
        // Then
        assertEquals(0L, result);
    }

    private void mockResultSetForBook() throws SQLException {
        when(resultSet.getLong(ID)).thenReturn(BOOK_ID);
        when(resultSet.getString(TITLE_COLUMN)).thenReturn(TITLE);
        when(resultSet.getString(AUTHOR_FIRST_NAME)).thenReturn(JOSHUA);
        when(resultSet.getString(AUTHOR_LAST_NAME)).thenReturn(BLOCH);
        when(resultSet.getString(GENRE_COLUMN)).thenReturn(GENRE);
        when(resultSet.getString(DESCRIPTION)).thenReturn(EFFECTIVE_JAVA);
        when(resultSet.getString(COVER_URL)).thenReturn(URL);
    }
}

