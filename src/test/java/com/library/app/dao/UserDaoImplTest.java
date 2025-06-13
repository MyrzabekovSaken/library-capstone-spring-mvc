package com.library.app.dao;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.impl.UserDaoImpl;
import com.library.app.model.Order;
import com.library.app.model.OrderStatus;
import com.library.app.model.Role;
import com.library.app.model.User;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {
    // 1. Константы
    private static final String ID = "id";
    private static final String USERNAME_COLUMN = "username";
    private static final String EMAIL_COLUMN = "email";
    private static final String PASSWORD_COLUMN = "password";
    private static final String STATUS_COLUMN = "status";
    private static final String ROLE_NAME = "role_name";
    private static final String UNKNOWN = "unknown";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String PENDING = "PENDING";
    private static final String ISSUED = "ISSUED";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String ORDER_STATUS = "order_status";
    private static final String ORDER_TYPE = "order_type";
    private static final String HOME = "HOME";
    private static final String READING_ROOM = "READING_ROOM";
    private static final String TITLE = "title";
    private static final String BOOK_A = "Book A";
    private static final String BOOK_B = "Book B";
    private static final String AUTHOR_FIRST_NAME = "author_first_name";
    private static final String JANE = "Jane";
    private static final String DOE = "Doe";
    private static final String AUTHOR_LAST_NAME = "author_last_name";
    private static final String INVENTORY_NUMBER = "inventory_number";
    private static final String INV_001 = "INV-001";
    private static final String INV_002 = "INV-002";
    private static final String JOHN = "john";
    private static final String JOHN_EMAIL = "john@example.com";
    private static final String PASSWORD = "secret";
    private static final String STATUS = "ACTIVE";
    private static final String DB_ERROR = "DB error";
    private static final String UPDATE_FAILED = "Update failed";
    private static final String DELETE_FAILED = "Delete failed";
    private static final String COUNT_FAILED = "Count failed";
    private static final Long USER_ID = 1L;

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
    @InjectMocks
    private UserDaoImpl testingInstance;

    @BeforeEach
    void setUp() throws Exception {
        mockedStatic = Mockito.mockStatic(ConnectionPool.class);
        mockedStatic.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        when(connectionPool.getConnection()).thenReturn(connection);
        testingInstance = new UserDaoImpl();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    // 4. Тест
    // POSITIVE TESTS

    @Test
    void shouldSaveUser() throws Exception {
        // Given
        User user = getUser();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // When
        testingInstance.save(user);
        // Then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldFindByUsername() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        prepareResultSetForUser();
        // When
        Optional<User> result = testingInstance.findByUsername(JOHN);
        // Then
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getId());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindUserById() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        prepareResultSetForUser();
        // When
        Optional<User> result = testingInstance.findById(USER_ID);
        // Then
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getId());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        prepareResultSetForUser();
        // When
        List<User> result = testingInstance.findAll();
        // Then
        assertEquals(1, result.size());
        assertEquals(JOHN, result.get(0).getUsername());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Given
        User user = getUser();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // When
        testingInstance.update(user);
        // Then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldRemoveUser() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.delete(USER_ID);
        // Then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldCountUserByStatus() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(5L);
        // When
        long count = testingInstance.countUserByStatus(STATUS);
        // Then
        assertEquals(5L, count);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldReturnMapOfReadersWithOrders() throws Exception {
        // Given
        prepareResultSetForActiveOrders();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // When
        Map<User, List<Order>> result = testingInstance.findReadersWithActiveOrders();
        // Then
        assertEquals(1, result.size());
        User user = result.keySet().iterator().next();
        assertEquals(JOHN, user.getUsername());
        assertEquals(JOHN_EMAIL, user.getEmail());
        List<Order> orders = result.get(user);
        assertEquals(2, orders.size());
        assertEquals(BOOK_A, orders.get(0).getBookCopy().getBook().getTitle());
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
        assertEquals(BOOK_B, orders.get(1).getBookCopy().getBook().getTitle());
        assertEquals(OrderStatus.ISSUED, orders.get(1).getStatus());
        verify(preparedStatement).executeQuery();
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotFindByUsernameWhenUserNotFound() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        // When
        Optional<User> result = testingInstance.findByUsername(UNKNOWN);
        // Then
        assertTrue(result.isEmpty());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldNotSaveUserWhenSQLExceptionOccurs() throws Exception {
        // Given
        User user = getUser();
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DB_ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.save(user));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindByUsernameWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DB_ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findByUsername(JOHN));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindUserByIdWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DB_ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findById(USER_ID));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindAllUsersWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DB_ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findAll());
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotUpdateUserWhenExceptionOccurs() throws Exception {
        // Given
        User user = getUser();
        when(connection.prepareStatement(any())).thenThrow(new SQLException(UPDATE_FAILED));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.update(user));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotRemoveUserWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DELETE_FAILED));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.delete(USER_ID));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotCountUserByStatusWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(COUNT_FAILED));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.countUserByStatus(STATUS));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindReadersWithActiveOrdersWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(DB_ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findReadersWithActiveOrders());
        verify(connection).prepareStatement(any());
    }

    private User getUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(JOHN);
        user.setEmail(JOHN_EMAIL);
        user.setPassword(PASSWORD);
        user.setStatus(STATUS);
        user.setRole(Role.READER);

        return user;
    }

    private void prepareResultSetForUser() throws SQLException {
        when(resultSet.getLong(ID)).thenReturn(USER_ID);
        when(resultSet.getString(USERNAME_COLUMN)).thenReturn(JOHN);
        when(resultSet.getString(EMAIL_COLUMN)).thenReturn(JOHN_EMAIL);
        when(resultSet.getString(PASSWORD_COLUMN)).thenReturn(PASSWORD);
        when(resultSet.getString(STATUS_COLUMN)).thenReturn(STATUS);
        when(resultSet.getString(ROLE_NAME)).thenReturn(Role.READER.name());
    }

    private void prepareResultSetForActiveOrders() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(USER_ID_COLUMN)).thenReturn(USER_ID, USER_ID);
        when(resultSet.getString(USERNAME_COLUMN)).thenReturn(JOHN, JOHN);
        when(resultSet.getString(EMAIL_COLUMN)).thenReturn(JOHN_EMAIL, JOHN_EMAIL);
        when(resultSet.getLong(ORDER_ID_COLUMN)).thenReturn(10L, 11L);
        when(resultSet.getString(ORDER_STATUS)).thenReturn(PENDING, ISSUED);
        when(resultSet.getString(ORDER_TYPE)).thenReturn(HOME, READING_ROOM);
        when(resultSet.getString(TITLE)).thenReturn(BOOK_A, BOOK_B);
        when(resultSet.getString(AUTHOR_FIRST_NAME)).thenReturn(JANE, JANE);
        when(resultSet.getString(AUTHOR_LAST_NAME)).thenReturn(DOE, DOE);
        when(resultSet.getString(INVENTORY_NUMBER)).thenReturn(INV_001, INV_002);
    }
}

