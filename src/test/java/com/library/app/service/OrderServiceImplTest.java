package com.library.app.service;

import com.library.app.dao.BookCopyDao;
import com.library.app.dao.OrderDao;
import com.library.app.dao.UserDao;
import com.library.app.dto.BookStatsDto;
import com.library.app.dto.UserStatsDto;
import com.library.app.model.*;
import com.library.app.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    // Константы
    private static final Long BOOK_ID = 1L;
    private static final Long ORDER_ID = 2L;
    private static final Long USER_ID = 3L;
    private static final Long COPY_ID = 4L;
    private static final String USERNAME = "reader1";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String NO_AVAILABLE_COPIES = "No available copies";
    private static final String ORDER_TYPE_MUST_NOT_BE_NULL = "Order type must not be null";
    private static final String RESERVED = "RESERVED";
    private static final String BOOK_TITLE = "Book Title";
    private static final String JOHN = "John";
    private static final String DOE = "Doe";
    private static final String JOHN_DOE = "John Doe";
    private static final String USER_1 = "user1";
    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String SOMEONE = "someone";
    private static final String UNAUTHORIZED_TO_CANCEL_THIS_ORDER = "Unauthorized to cancel this order";
    private static final String ONLY_PENDING_ORDERS_CAN_BE_CANCELED = "Only pending orders can be canceled";
    private static final String ORDER_IS_NOT_IN_PENDING_STATUS = "Order is not in PENDING status";
    private static final String ONLY_ISSUED_ORDERS_CAN_BE_RETURNED = "Only ISSUED orders can be returned";

    // Моки
    @Mock
    private OrderDao orderDao;

    @Mock
    private UserDao userDao;

    @Mock
    private BookCopyDao bookCopyDao;

    // @InjectMocks
    @InjectMocks
    private OrderServiceImpl testingInstance;

    // Тесты
    // POSITIVE TESTS

    @Test
    void shouldCreateOrder() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(bookCopyDao.findAvailableCopy(BOOK_ID)).thenReturn(Optional.of(copy));
        // When
        testingInstance.createOrder(BOOK_ID, USERNAME, OrderType.HOME);
        // Then
        assertEquals(CopyStatus.RESERVED, copy.getStatus());
        verify(userDao).findByUsername(USERNAME);
        verify(bookCopyDao).findAvailableCopy(BOOK_ID);
        verify(orderDao).save(any(Order.class));
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldCreateOrderForHome() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(bookCopyDao.findAvailableCopy(BOOK_ID)).thenReturn(Optional.of(copy));
        // When
        testingInstance.createOrder(BOOK_ID, USERNAME, OrderType.HOME);
        // Then
        verify(orderDao).save(argThat(order ->
                order.getDueDate().equals(LocalDate.now().plusDays(14))));
        verify(userDao).findByUsername(USERNAME);
        verify(bookCopyDao).findAvailableCopy(BOOK_ID);
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldCreateOrderForReadingRoom() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(bookCopyDao.findAvailableCopy(BOOK_ID)).thenReturn(Optional.of(copy));
        // When
        testingInstance.createOrder(BOOK_ID, USERNAME, OrderType.READING_ROOM);
        // Then
        verify(orderDao).save(argThat(order ->
                order.getDueDate().equals(LocalDate.now().plusDays(1))));
        verify(userDao).findByUsername(USERNAME);
        verify(bookCopyDao).findAvailableCopy(BOOK_ID);
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldGetOrdersByUsername() {
        // Given
        List<Order> orders = List.of(new Order());
        when(orderDao.findByUsername(USERNAME)).thenReturn(orders);
        // When
        List<Order> result = testingInstance.getOrdersByUsername(USERNAME);
        // Then
        assertEquals(orders, result);
        verify(orderDao).findByUsername(USERNAME);
    }

    @Test
    void shouldGetOrderIssuedOrReserved() {
        // Given
        when(orderDao.findIssuedOrReserved(COPY_ID)).thenReturn(Optional.of(RESERVED));
        // When
        Optional<String> result = testingInstance.getIssuedOrReserved(COPY_ID);
        // Then
        assertTrue(result.isPresent());
        assertEquals(RESERVED, result.get());
        verify(orderDao).findIssuedOrReserved(COPY_ID);
    }

    @Test
    void shouldOrderCountByStatuses() {
        // Given
        List<OrderStatus> statuses = List.of(OrderStatus.ISSUED, OrderStatus.PENDING);
        when(orderDao.countOrderStatus(statuses)).thenReturn(7L);
        // When
        long result = testingInstance.getCountByStatuses(statuses);
        // Then
        assertEquals(7L, result);
        verify(orderDao).countOrderStatus(statuses);
    }

    @Test
    void shouldGetTopRequestedBooks() {
        // Given
        Object[] row = new Object[6];
        row[1] = BOOK_TITLE;
        row[2] = JOHN;
        row[3] = DOE;
        row[5] = 7L;
        List<Object[]> data = new ArrayList<>();
        data.add(row);
        when(orderDao.findTopRequestedBooks(3)).thenReturn(data);
        // When
        List<BookStatsDto> result = testingInstance.getTopRequestedBooks(3);
        // Then
        assertEquals(1, result.size());
        assertEquals(BOOK_TITLE, result.get(0).getTitle());
        assertEquals(JOHN_DOE, result.get(0).getAuthorFullName());
        assertEquals(7L, result.get(0).getRequestCount());
        verify(orderDao).findTopRequestedBooks(3);
    }

    @Test
    void shouldGetTopActiveUsers() {
        // Given
        Object[] row = new Object[3];
        row[1] = USER_1;
        row[2] = 10L;
        List<Object[]> data = new ArrayList<>();
        data.add(row);
        when(orderDao.findTopActiveUsers(2)).thenReturn(data);
        // When
        List<UserStatsDto> result = testingInstance.getTopActiveUsers(2);
        // Then
        assertEquals(1, result.size());
        assertEquals(USER_1, result.get(0).getUsername());
        assertEquals(10L, result.get(0).getRequestCount());
        verify(orderDao).findTopActiveUsers(2);
    }

    @Test
    void shouldGetActiveOrderForBook() {
        // Given
        when(orderDao.hasActiveOrderForBook(BOOK_ID, USER_ID)).thenReturn(true);
        // When
        boolean result = testingInstance.getActiveOrderForBook(BOOK_ID, USER_ID);
        // Then
        assertTrue(result);
        verify(orderDao).hasActiveOrderForBook(BOOK_ID, USER_ID);
    }

    @Test
    void shouldCancelPendingOrder() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        Order order = getOrderWith(copy, OrderStatus.PENDING, user);
        order.setUser(getUser());
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // When
        testingInstance.cancelOrder(ORDER_ID, USERNAME);
        // Then
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertNull(order.getDueDate());
        assertEquals(CopyStatus.AVAILABLE, copy.getStatus());
        verify(orderDao).update(order);
        verify(orderDao).findById(ORDER_ID);
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldUpdateOrderAndBook() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        Order order = getOrderWith(copy, OrderStatus.PENDING, user);
        LocalDate dueDate = LocalDate.now().plusDays(5);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // When
        testingInstance.confirmOrderIssue(ORDER_ID, dueDate);
        // Then
        assertEquals(OrderStatus.ISSUED, order.getStatus());
        assertEquals(dueDate, order.getDueDate());
        assertEquals(CopyStatus.ISSUED, copy.getStatus());
        verify(orderDao).findById(ORDER_ID);
        verify(orderDao).update(order);
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldMarkAsReturnedOrder() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        Order order = getOrderWith(copy, OrderStatus.ISSUED, user);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // When
        testingInstance.markAsReturned(ORDER_ID);
        // Then
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(LocalDate.now(), order.getReturnDate());
        assertEquals(CopyStatus.AVAILABLE, copy.getStatus());
        verify(orderDao).findById(ORDER_ID);
        verify(orderDao).update(order);
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldGetAllOrders() {
        // Given
        Order order = new Order();
        List<Order> expectedOrders = List.of(order);
        when(orderDao.findAllOrders()).thenReturn(expectedOrders);
        // When
        List<Order> result = testingInstance.getAllOrders();
        // Then
        assertEquals(expectedOrders, result);
        verify(orderDao).findAllOrders();
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotCreateOrderWhenUserNotFound() {
        // Given
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.createOrder(BOOK_ID, USERNAME, OrderType.HOME));
        assertEquals(USER_NOT_FOUND, exception.getMessage());
        verify(userDao).findByUsername(USERNAME);
    }

    @Test
    void shouldNotCreateOrderWhenNoAvailableCopy() {
        // Given
        User user = getUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(bookCopyDao.findAvailableCopy(BOOK_ID)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.createOrder(BOOK_ID, USERNAME, OrderType.HOME));
        assertEquals(NO_AVAILABLE_COPIES, exception.getMessage());
        verify(userDao).findByUsername(USERNAME);
        verify(bookCopyDao).findAvailableCopy(BOOK_ID);
    }

    @Test
    void shouldNotCreateOrderWhenOrderTypeIsNull() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(bookCopyDao.findAvailableCopy(BOOK_ID)).thenReturn(Optional.of(copy));
        // Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                testingInstance.createOrder(BOOK_ID, USERNAME, null));
        assertEquals(ORDER_TYPE_MUST_NOT_BE_NULL, exception.getMessage());
        verify(userDao).findByUsername(USERNAME);
        verify(bookCopyDao).findAvailableCopy(BOOK_ID);
    }

    @Test
    void shouldNotOrderWhenOrderNotFound() {
        // Given
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.cancelOrder(ORDER_ID, USERNAME));
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotOrderWhenUserTriesToCancelSomeoneElseOrder() {
        // Given
        User otherUser = new User();
        otherUser.setUsername(SOMEONE);
        BookCopy copy = getCopy();
        Order order = getOrderWith(copy, OrderStatus.PENDING, otherUser);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.cancelOrder(ORDER_ID, USERNAME));
        assertEquals(UNAUTHORIZED_TO_CANCEL_THIS_ORDER, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotOrderWhenOrderIsNotPending() {
        // Given
        User user = getUser();
        BookCopy copy = getCopy();
        Order order = getOrderWith(copy, OrderStatus.ISSUED, user);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.cancelOrder(ORDER_ID, USERNAME));
        assertEquals(ONLY_PENDING_ORDERS_CAN_BE_CANCELED, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotConfirmOrderWhenOrderNotFound() {
        // Given
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.confirmOrderIssue(ORDER_ID, LocalDate.now()));
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotConfirmOrderWhenStatusIsNotPending() {
        // Given
        Order order = new Order();
        order.setStatus(OrderStatus.ISSUED);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.confirmOrderIssue(ORDER_ID, LocalDate.now()));
        assertEquals(ORDER_IS_NOT_IN_PENDING_STATUS, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotMarkAsReturnedWhenOrderNotFound() {
        // Given
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.markAsReturned(ORDER_ID));
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }

    @Test
    void shouldNotMarkAsReturnedWhenOrderNotIssued() {
        // Given
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.markAsReturned(ORDER_ID));
        assertEquals(ONLY_ISSUED_ORDERS_CAN_BE_RETURNED, exception.getMessage());
        verify(orderDao).findById(ORDER_ID);
    }


    private static User getUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        return user;
    }

    private static BookCopy getCopy() {
        BookCopy copy = new BookCopy();
        copy.setId(COPY_ID);
        copy.setStatus(CopyStatus.AVAILABLE);

        return copy;
    }

    private static Order getOrderWith(BookCopy copy, OrderStatus status, User user) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(status);
        order.setBookCopy(copy);
        order.setUser(user);

        return order;
    }
}
