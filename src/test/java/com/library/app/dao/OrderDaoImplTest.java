package com.library.app.dao;

import com.library.app.config.ConnectionPool;
import com.library.app.dao.impl.OrderDaoImpl;
import com.library.app.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDaoImplTest {
    // 1. Константы
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long BOOK_ID = 3L;
    private static final Long COPY_ID = 4L;
    private static final String HOME = "HOME";
    private static final String ISSUED = "ISSUED";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String ORDER_TYPE = "order_type";
    private static final String ORDER_STATUS = "order_status";
    private static final String ISSUE_DATE_COLUMN = "issue_date";
    private static final String DUE_DATE_COLUMN = "due_date";
    private static final String RETURN_DATE = "return_date";
    private static final String ISSUE_DATE_JUNE = "2025-06-01";
    private static final String DUE_DATE_JUNE = "2025-06-15";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String USERNAME_COLUMN = "username";
    private static final String BOOK_ID_COLUMN = "book_id";
    private static final String TITLE_COLUMN = "title";
    private static final String BOOK_TITLE = "Book Title";
    private static final String AUTHOR_FIRST_NAME = "author_first_name";
    private static final String JANE = "Jane";
    private static final String AUTHOR_LAST_NAME = "author_last_name";
    private static final String DOE = "Doe";
    private static final String COPY_ID_COLUMN = "copy_id";
    private static final String INVENTORY_NUMBER = "inventory_number";
    private static final String ID = "id";
    private static final String REQUEST_COUNT = "request_count";
    private static final String ORDER_COUNT = "order_count";
    private static final String USERNAME = "user1";
    private static final String ERROR = "Error";

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
    private OrderDaoImpl testingInstance;

    @BeforeEach
    void setUp() throws Exception {
        mockedStatic = Mockito.mockStatic(ConnectionPool.class);
        mockedStatic.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        when(connectionPool.getConnection()).thenReturn(connection);
        testingInstance = new OrderDaoImpl();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    // POSITIVE TESTS

    @Test
    void shouldSaveOrder() throws Exception {
        // Given
        Order order = getOrder();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.save(order);
        // Then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        // Given
        Order order = getOrder();
        order.setId(ORDER_ID);
        order.setReturnDate(null);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        // When
        testingInstance.update(order);
        // Then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void shouldFindOrderById() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        mockResultSet(ID);
        // When
        Optional<Order> result = testingInstance.findById(ORDER_ID);
        // Then
        assertTrue(result.isPresent());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindOrderByUsername() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        mockResultSet(ORDER_ID_COLUMN);
        // When
        List<Order> orders = testingInstance.findByUsername(USERNAME);
        // Then
        assertEquals(1, orders.size());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindUsernameWithIssuedOrReserved() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(USERNAME_COLUMN)).thenReturn(USERNAME);
        // When
        Optional<String> result = testingInstance.findIssuedOrReserved(ORDER_ID);
        // Then
        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldCountIssuedOrderStatus() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(7L);
        // When
        long result = testingInstance.countOrderStatus(List.of(OrderStatus.ISSUED));
        // Then
        assertEquals(7L, result);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindTopRequestedBooks() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(ID)).thenReturn(ORDER_ID);
        when(resultSet.getString(TITLE_COLUMN)).thenReturn(BOOK_TITLE);
        when(resultSet.getString(AUTHOR_FIRST_NAME)).thenReturn(AUTHOR_FIRST_NAME);
        when(resultSet.getString(AUTHOR_LAST_NAME)).thenReturn(AUTHOR_LAST_NAME);
        when(resultSet.getLong(REQUEST_COUNT)).thenReturn(10L);
        // When
        List<Object[]> result = testingInstance.findTopRequestedBooks(5);
        // Then
        assertEquals(1, result.size());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindTopActiveUsers() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(ID)).thenReturn(BOOK_ID);
        when(resultSet.getString(USERNAME_COLUMN)).thenReturn(USERNAME);
        when(resultSet.getLong(ORDER_COUNT)).thenReturn(5L);
        // When
        List<Object[]> result = testingInstance.findTopActiveUsers(5);
        // Then
        assertEquals(1, result.size());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldCheckHasActiveOrderForBook() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // When
        boolean result = testingInstance.hasActiveOrderForBook(USER_ID, BOOK_ID);
        // Then
        assertTrue(result);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void shouldFindAllOrders() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        mockResultSet(ID);
        // When
        List<Order> result = testingInstance.findAllOrders();
        // Then
        assertEquals(1, result.size());
        verify(preparedStatement).executeQuery();
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotSaveOrderWhenExceptionOccurs() throws Exception {
        // Given
        Order order = getOrder();
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.save(order));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotUpdateOrderWhenExceptionOccurs() throws Exception {
        // Given
        Order order = getOrder();
        order.setId(ORDER_ID);
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.update(order));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindOrderByIdWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findById(ORDER_ID));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindByUsernameWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findByUsername(USERNAME));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindIssuedOrReservedOrderWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findIssuedOrReserved(ORDER_ID));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotCountOrderStatusWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.countOrderStatus(List.of(OrderStatus.ISSUED)));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindTopRequestedBooksWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findTopRequestedBooks(5));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindTopActiveUsersWhenSQLExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findTopActiveUsers(5));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotCheckHasActiveOrderForBookWhenExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.hasActiveOrderForBook(USER_ID, BOOK_ID));
        verify(connection).prepareStatement(any());
    }

    @Test
    void shouldNotFindAllOrdersWhenSQLExceptionOccurs() throws Exception {
        // Given
        when(connection.prepareStatement(any())).thenThrow(new SQLException(ERROR));
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.findAllOrders());
        verify(connection).prepareStatement(any());
    }

    private static Order getOrder() {
        User user = new User();
        user.setId(USER_ID);
        Book book = new Book();
        book.setId(BOOK_ID);
        BookCopy copy = new BookCopy();
        copy.setId(COPY_ID);
        copy.setBook(book);
        Order order = new Order();
        order.setUser(user);
        order.setBookCopy(copy);
        order.setType(OrderType.HOME);
        order.setStatus(OrderStatus.ISSUED);
        order.setIssueDate(LocalDate.of(2025, 1, 1));
        order.setDueDate(LocalDate.of(2025, 1, 14));

        return order;
    }

    private void mockResultSet(String idAlias) throws SQLException {
        when(resultSet.getLong(idAlias)).thenReturn(ORDER_ID);
        when(resultSet.getString(ORDER_TYPE)).thenReturn(HOME);
        when(resultSet.getString(ORDER_STATUS)).thenReturn(ISSUED);
        when(resultSet.getDate(ISSUE_DATE_COLUMN)).thenReturn(Date.valueOf(ISSUE_DATE_JUNE));
        when(resultSet.getDate(DUE_DATE_COLUMN)).thenReturn(Date.valueOf(DUE_DATE_JUNE));
        when(resultSet.getDate(RETURN_DATE)).thenReturn(null);
        when(resultSet.getLong(USER_ID_COLUMN)).thenReturn(USER_ID);
        when(resultSet.getString(USERNAME_COLUMN)).thenReturn(USERNAME);
        when(resultSet.getLong(BOOK_ID_COLUMN)).thenReturn(BOOK_ID);
        when(resultSet.getString(TITLE_COLUMN)).thenReturn(BOOK_TITLE);
        when(resultSet.getString(AUTHOR_FIRST_NAME)).thenReturn(JANE);
        when(resultSet.getString(AUTHOR_LAST_NAME)).thenReturn(DOE);
        when(resultSet.getLong(COPY_ID_COLUMN)).thenReturn(COPY_ID);
        when(resultSet.getString(INVENTORY_NUMBER)).thenReturn(INVENTORY_NUMBER);
    }
}
