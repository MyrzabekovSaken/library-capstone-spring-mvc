package com.library.app.service.impl;

import com.library.app.dao.BookCopyDao;
import com.library.app.dao.OrderDao;
import com.library.app.dao.UserDao;
import com.library.app.dto.BookStatsDto;
import com.library.app.dto.UserStatsDto;
import com.library.app.mapper.BookStatsMapper;
import com.library.app.mapper.UserStatsMapper;
import com.library.app.model.*;
import com.library.app.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing book orders.
 */
@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final String UNKNOWN_ORDER_TYPE_LOG = "Unknown order type: %s";
    private static final String USER_NOT_FOUND_WHEN_CREATING_ORDER = "User '{}' not found when creating order";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String NO_AVAILABLE_COPIES_FOR_BOOK_ID = "No available copies for bookId={}";
    private static final String NO_AVAILABLE_COPIES = "No available copies";
    private static final String ORDER_NOT_FOUND_WITH_ID = "Order not found with id={}";
    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String ATTEMPT_TO_RETURN_ORDER = "Attempt to return order not in ISSUED status: id={}, status={}";
    private static final String ONLY_ISSUED_ORDERS_CAN_BE_RETURNED = "Only ISSUED orders can be returned";
    private static final String ORDER_NOT_FOUND_WITH_ID_REQUESTED_BY_USER =
            "Order not found with id={}, requested by user={}";
    private static final String USER_TRIED_TO_CANCEL_SOMEONE_ELSE_ORDER_ID =
            "User '{}' tried to cancel someone else's order ID={}";
    private static final String UNAUTHORIZED_TO_CANCEL_THIS_ORDER = "Unauthorized to cancel this order";
    private static final String USER_TRIED_TO_CANCEL_NON_PENDING_ORDER =
            "User '{}' tried to cancel non-pending order ID={} with status={}";
    private static final String ONLY_PENDING_ORDERS_CAN_BE_CANCELED = "Only pending orders can be canceled";
    private static final String ORDER_NOT_FOUND_WITH_ID_AND_DUE_DATE = "Order not found with id={} and due date={}";
    private static final String LIBRARIAN_TRIED_TO_CONFIRM_ORDER =
            "Librarian tried to confirm order ID={} which is not in PENDING state, current status: {}";
    private static final String ORDER_IS_NOT_IN_PENDING_STATUS = "Order is not in PENDING status";
    public static final String ORDER_TYPE_MUST_NOT_BE_NULL = "Order type must not be null";

    private final OrderDao orderDao;
    private final UserDao userDao;
    private final BookCopyDao bookCopyDao;

    /**
     * Constructs an {@code OrderServiceImpl} with necessary DAOs for managing book orders.
     *
     * @param orderDao    the DAO responsible for order management
     * @param userDao     the DAO for user-related operations
     * @param bookCopyDao the DAO for managing book copies
     */
    @Autowired
    public OrderServiceImpl(OrderDao orderDao, UserDao userDao, BookCopyDao bookCopyDao) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.bookCopyDao = bookCopyDao;
    }

    /**
     * Creates a new book order for a given user.
     *
     * @param bookId   the ID of the book
     * @param username the username of the requester
     * @param type     the type of order (HOME or READING_ROOM)
     * @throws RuntimeException if the user does not exist or if no available book copies are found
     */

    @Override
    public void createOrder(Long bookId, String username, OrderType type) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn(USER_NOT_FOUND_WHEN_CREATING_ORDER, username);
                    return new RuntimeException(USER_NOT_FOUND);
                });

        BookCopy copy = bookCopyDao.findAvailableCopy(bookId)
                .orElseThrow(() -> {
                    logger.warn(NO_AVAILABLE_COPIES_FOR_BOOK_ID, bookId);
                    return new RuntimeException(NO_AVAILABLE_COPIES);
                });

        LocalDate today = LocalDate.now();
        Order order = buildOrder(user, copy, type, today);
        orderDao.save(order);
        copy.setStatus(CopyStatus.RESERVED);
        bookCopyDao.update(copy);
    }

    /**
     * Retrieves all orders placed by the given user.
     *
     * @param username the username of the user
     * @return list of orders
     */
    @Override
    public List<Order> getOrdersByUsername(String username) {
        return orderDao.findByUsername(username);
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return list of orders
     */
    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAllOrders();
    }

    /**
     * Marks the order as returned and updates the book copy status.
     *
     * @param orderId the ID of the returned order
     * @throws RuntimeException if the order is not found or is not in ISSUED status
     */
    @Override
    public void markAsReturned(Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn(ORDER_NOT_FOUND_WITH_ID, orderId);

                    return new RuntimeException(ORDER_NOT_FOUND);
                });

        if (order.getStatus() != OrderStatus.ISSUED) {
            logger.warn(ATTEMPT_TO_RETURN_ORDER,
                    orderId, order.getStatus());
            throw new RuntimeException(ONLY_ISSUED_ORDERS_CAN_BE_RETURNED);
        }

        order.setStatus(OrderStatus.RETURNED);
        order.setReturnDate(LocalDate.now());
        orderDao.update(order);

        BookCopy copy = order.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyDao.update(copy);
    }

    /**
     * Finds whether the copy is issued or reserved.
     *
     * @param copyId the ID of the book copy
     * @return optional status string
     */
    @Override
    public Optional<String> getIssuedOrReserved(Long copyId) {
        return orderDao.findIssuedOrReserved(copyId);
    }

    /**
     * Counts orders by their statuses.
     *
     * @param statuses list of order statuses to count
     * @return count
     */
    @Override
    public long getCountByStatuses(List<OrderStatus> statuses) {
        return orderDao.countOrderStatus(statuses);
    }

    /**
     * Returns top requested books.
     *
     * @param limit maximum number of books to return
     * @return a list of BookStatsDto objects representing the top requested books
     */
    @Override
    public List<BookStatsDto> getTopRequestedBooks(int limit) {
        return orderDao.findTopRequestedBooks(limit).stream()
                .map(BookStatsMapper::toDto)
                .toList();
    }

    /**
     * Returns top active users based on order count.
     *
     * @param limit maximum number of users to return
     * @return a list of UserStatsDto objects representing the top active users
     */
    @Override
    public List<UserStatsDto> getTopActiveUsers(int limit) {
        return orderDao.findTopActiveUsers(limit).stream()
                .map(UserStatsMapper::toDto)
                .toList();
    }

    /**
     * Checks if the user has an active order for a specific book.
     *
     * @param bookId the ID of the book
     * @param userId the ID of the user
     * @return true if active order exists, false otherwise
     */
    @Override
    public boolean getActiveOrderForBook(Long bookId, Long userId) {
        return orderDao.hasActiveOrderForBook(bookId, userId);
    }

    /**
     * Cancels a pending order for the user.
     *
     * @param orderId the ID of the order to cancel
     * @param name    the username of the user attempting to cancel
     * @throws RuntimeException if the order is not found, does not belong to the user, or is not in PENDING status
     */
    @Override
    public void cancelOrder(Long orderId, String name) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn(ORDER_NOT_FOUND_WITH_ID_REQUESTED_BY_USER, orderId, name);
                    return new RuntimeException(ORDER_NOT_FOUND);
                });

        if (!order.getUser().getUsername().equals(name)) {
            logger.warn(USER_TRIED_TO_CANCEL_SOMEONE_ELSE_ORDER_ID, name, orderId);
            throw new RuntimeException(UNAUTHORIZED_TO_CANCEL_THIS_ORDER);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn(USER_TRIED_TO_CANCEL_NON_PENDING_ORDER,
                    name, orderId, order.getStatus());
            throw new RuntimeException(ONLY_PENDING_ORDERS_CAN_BE_CANCELED);
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setDueDate(null);
        orderDao.update(order);

        BookCopy copy = order.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyDao.update(copy);
    }

    /**
     * Confirms order issuance and updates book copy status.
     *
     * @param orderId the ID of the order to confirm
     * @param dueDate the date the book is due for return
     * @throws RuntimeException if the order is not found or is not in PENDING status
     */
    @Override
    public void confirmOrderIssue(Long orderId, LocalDate dueDate) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn(ORDER_NOT_FOUND_WITH_ID_AND_DUE_DATE, orderId, dueDate);
                    return new RuntimeException(ORDER_NOT_FOUND);
                });

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn(LIBRARIAN_TRIED_TO_CONFIRM_ORDER,
                    orderId, order.getStatus());
            throw new RuntimeException(ORDER_IS_NOT_IN_PENDING_STATUS);
        }

        order.setStatus(OrderStatus.ISSUED);
        order.setDueDate(dueDate);
        orderDao.update(order);

        BookCopy copy = order.getBookCopy();
        copy.setStatus(CopyStatus.ISSUED);
        bookCopyDao.update(copy);
    }

    /**
     * Builds an order object with calculated due date.
     *
     * @param user      the user placing the order
     * @param copy      the book copy being ordered
     * @param type      the type of order (HOME or READING_ROOM)
     * @param issueDate the date the order is issued
     * @return a new Order object with the specified details and calculated due date
     * @throws IllegalArgumentException if the order type is unknown
     */
    private Order buildOrder(User user, BookCopy copy, OrderType type, LocalDate issueDate) {
        if (type != null) {
            LocalDate dueDate;

            switch (type) {
                case HOME:
                    dueDate = issueDate.plusDays(14);
                    break;
                case READING_ROOM:
                    dueDate = issueDate.plusDays(1);
                    break;
                default:
                    String message = String.format(UNKNOWN_ORDER_TYPE_LOG, type);
                    logger.error(message);
                    throw new IllegalArgumentException(message);
            }

            Order order = new Order();
            order.setUser(user);
            order.setBookCopy(copy);
            order.setType(type);
            order.setStatus(OrderStatus.PENDING);
            order.setIssueDate(issueDate);
            order.setDueDate(dueDate);

            return order;
        }
        String message = ORDER_TYPE_MUST_NOT_BE_NULL;
        logger.error(message);
        throw new IllegalArgumentException(message);
    }
}
