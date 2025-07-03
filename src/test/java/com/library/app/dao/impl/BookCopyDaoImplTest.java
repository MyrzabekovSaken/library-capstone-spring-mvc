package com.library.app.dao.impl;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.BookDao;
import com.library.app.model.Book;
import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCopyDaoImplTest {
    // 1. Константы
    private static final Long COPY_ID = 1L;
    private static final Long BOOK_ID_LONG = 2L;
    private static final String ID = "id";
    private static final String INVENTORY_NUMBER = "inventory_number";
    private static final String BOOK_ID = "book_id";
    private static final String STATUS = "status";
    private static final String AVAILABLE = "AVAILABLE";
    private static final String ISSUED = "ISSUED";
    private static final String INV_0001 = "INV-0001";
    private static final String INV_0002 = "INV-0002";
    private static final String INV_9999 = "INV-9999";

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

    @Mock
    private BookDao bookDao;

    // 3.
    @InjectMocks
    private BookCopyDaoImpl testingInstance;

    @BeforeEach
    void setUp() throws Exception {
        mockedStatic = Mockito.mockStatic(ConnectionPool.class);
        mockedStatic.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        when(connectionPool.getConnection()).thenReturn(connection);
        testingInstance = new BookCopyDaoImpl(bookDao);
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    // 4. Тест
    // POSITIVE TESTS

    @Test
    void shouldFindById() throws Exception {
        // Given
        prepareResultSetForCopy(INV_0001, BOOK_ID_LONG, AVAILABLE);
        when(resultSet.next()).thenReturn(true);
        Book expectedBook = new Book();
        expectedBook.setId(BOOK_ID_LONG);
        when(bookDao.findById(BOOK_ID_LONG)).thenReturn(Optional.of(expectedBook));
        // When
        Optional<BookCopy> result = testingInstance.findById(COPY_ID);
        // Then
        verify(preparedStatement).executeQuery();
        assertTrue(result.isPresent());
        assertEquals(COPY_ID, result.get().getId());
        assertEquals(CopyStatus.AVAILABLE, result.get().getStatus());
        assertEquals(INV_0001, result.get().getInventoryNumber());
    }

    @Test
    void shouldCountAvailableCopies() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);
        // When
        int result = testingInstance.countAvailableCopies(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).executeQuery();
        assertEquals(3, result);
    }

    @Test
    void shouldFindAvailableCopy() throws Exception {
        // Given
        prepareResultSetForCopy(INV_0002, BOOK_ID_LONG, AVAILABLE);
        when(resultSet.next()).thenReturn(true);
        Book book = new Book();
        book.setId(BOOK_ID_LONG);
        when(bookDao.findById(BOOK_ID_LONG)).thenReturn(Optional.of(book));
        // When
        Optional<BookCopy> result = testingInstance.findAvailableCopy(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).executeQuery();
        assertTrue(result.isPresent());
        assertEquals(INV_0002, result.get().getInventoryNumber());
    }

    @Test
    void shouldFindAllByBookId() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(ID)).thenReturn(1L, 2L);
        when(resultSet.getString(INVENTORY_NUMBER)).thenReturn(INV_0001, INV_0002);
        when(resultSet.getLong(BOOK_ID)).thenReturn(BOOK_ID_LONG, BOOK_ID_LONG);
        when(resultSet.getString(STATUS)).thenReturn(AVAILABLE, ISSUED);
        Book book = new Book();
        book.setId(BOOK_ID_LONG);
        when(bookDao.findById(BOOK_ID_LONG)).thenReturn(Optional.of(book));
        // When
        List<BookCopy> result = testingInstance.findAllByBookId(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).executeQuery();
        assertEquals(2, result.size());
        assertEquals(INV_0001, result.get(0).getInventoryNumber());
    }

    @Test
    void shouldCallInsertStatement() throws Exception {
        // Given
        BookCopy copy = new BookCopy(null, INVENTORY_NUMBER,
                new Book(BOOK_ID_LONG,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                CopyStatus.AVAILABLE);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // When
        testingInstance.save(copy);
        // Then
        verify(preparedStatement).setLong(1, BOOK_ID_LONG);
        verify(preparedStatement).setString(2, INVENTORY_NUMBER);
        verify(preparedStatement).setString(3, AVAILABLE);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldUpdateStatement() throws Exception {
        // Given
        BookCopy copy = new BookCopy(COPY_ID, INV_9999, null, CopyStatus.ISSUED);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // When
        testingInstance.update(copy);
        // Then
        verify(preparedStatement).setString(1, INV_9999);
        verify(preparedStatement).setString(2, ISSUED);
        verify(preparedStatement).setLong(3, COPY_ID);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldDeleteStatement() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        // When
        testingInstance.delete(COPY_ID);
        // Then
        verify(preparedStatement).setLong(1, COPY_ID);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldFindLastInventoryNumber() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(INVENTORY_NUMBER)).thenReturn(INV_0001);
        // When
        Optional<String> result = testingInstance.findLastInventoryNumber(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).setLong(1, BOOK_ID_LONG);
        verify(preparedStatement).executeQuery();
        assertTrue(result.isPresent());
        assertEquals(INV_0001, result.get());
    }

    @Test
    void shouldCountAllBookCopy() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(42L);
        // When
        long result = testingInstance.countAllBookCopy();
        // Then
        verify(preparedStatement).executeQuery();
        assertEquals(42L, result);
    }

    @Test
    void shouldCountBookCopyStatus() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(5L);
        // When
        long result = testingInstance.countBookCopyStatus(CopyStatus.AVAILABLE);
        // Then
        verify(preparedStatement).setString(1, AVAILABLE);
        verify(preparedStatement).executeQuery();
        assertEquals(5L, result);
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotFindByIdWhenNoResult() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        Optional<BookCopy> result = testingInstance.findById(COPY_ID);
        // Then
        verify(preparedStatement).executeQuery();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFindByIdWhenException() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        // When
        Optional<BookCopy> result = testingInstance.findById(COPY_ID);
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotCountAvailableCopiesWhenException() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        // When
        int result = testingInstance.countAvailableCopies(BOOK_ID_LONG);
        // Then
        assertEquals(0, result);
    }

    @Test
    void shouldNotFindAvailableCopyWhenNoCopy() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        Optional<BookCopy> result = testingInstance.findAvailableCopy(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotSaveWhenSQLExceptionOccurs() throws Exception {
        // Given
        BookCopy copy = new BookCopy(null, INVENTORY_NUMBER,
                new Book(BOOK_ID_LONG,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                CopyStatus.RESERVED);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException());
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.save(copy));
    }

    @Test
    void shouldNotFindLastInventoryNumberWhenNoResult() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        Optional<String> result = testingInstance.findLastInventoryNumber(BOOK_ID_LONG);
        // Then
        verify(preparedStatement).setLong(1, BOOK_ID_LONG);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFindLastInventoryNumberWhenException() throws Exception {
        // Given
        when(connectionPool.getConnection()).thenThrow(new SQLException());
        // When
        Optional<String> result = testingInstance.findLastInventoryNumber(BOOK_ID_LONG);
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotCountAllBookCopyWhenNoResult() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        long result = testingInstance.countAllBookCopy();
        // Then
        verify(preparedStatement).executeQuery();
        assertEquals(0L, result);
    }

    @Test
    void shouldCountAllBookCopyWhenException() throws Exception {
        // Given
        when(connectionPool.getConnection()).thenThrow(new SQLException());
        // When
        long result = testingInstance.countAllBookCopy();
        // Then
        assertEquals(0L, result);
    }

    @Test
    void shouldNotCountBookCopyStatusWhenNoResult() throws Exception {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        long result = testingInstance.countBookCopyStatus(CopyStatus.ISSUED);
        // Then
        verify(preparedStatement).setString(1, ISSUED);
        assertEquals(0L, result);
    }

    @Test
    void shouldNotCountBookCopyStatusWhenException() throws Exception {
        // Given
        when(connectionPool.getConnection()).thenThrow(new SQLException());
        // When
        long result = testingInstance.countBookCopyStatus(CopyStatus.AVAILABLE);
        // Then
        assertEquals(0L, result);
    }

    private void prepareResultSetForCopy(String inventoryNumberValue, Long bookIdValue, String statusValue)
            throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getLong(ID)).thenReturn(COPY_ID);
        when(resultSet.getString(INVENTORY_NUMBER)).thenReturn(inventoryNumberValue);
        when(resultSet.getLong(BOOK_ID)).thenReturn(bookIdValue);
        when(resultSet.getString(STATUS)).thenReturn(statusValue);
    }
}
