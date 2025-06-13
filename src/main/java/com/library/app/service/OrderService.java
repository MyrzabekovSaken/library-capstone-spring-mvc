package com.library.app.service;

import com.library.app.dto.BookStatsDto;
import com.library.app.dto.UserStatsDto;
import com.library.app.model.Order;
import com.library.app.model.OrderStatus;
import com.library.app.model.OrderType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing book orders (requests).
 */
public interface OrderService {
    /**
     * Creates a new order for a book.
     *
     * @param bookId   the ID of the book
     * @param username the username of the requester
     * @param type     the type of order (HOME or READING_ROOM)
     */
    void createOrder(Long bookId, String username, OrderType type);

    /**
     * Cancels an order by ID.
     *
     * @param orderId the ID of the order to cancel
     * @param name    the username of the user attempting to cancel
     */
    void cancelOrder(Long orderId, String name);

    /**
     * Confirms issuance of a book copy and sets due date.
     *
     * @param orderId the ID of the order to confirm
     * @param dueDate the date the book is due for return
     */
    void confirmOrderIssue(Long orderId, LocalDate dueDate);

    /**
     * Marks the book order as returned.
     *
     * @param orderId the ID of the returned order
     */
    void markAsReturned(Long orderId);

    /**
     * Counts the total number of orders with the specified statuses.
     *
     * @param statuses list of order statuses to count
     * @return total count of orders
     */
    long getCountByStatuses(List<OrderStatus> statuses);

    /**
     * Checks if the user has an active order for the specified book.
     *
     * @param bookId the ID of the book
     * @param userId the ID of the user
     * @return true if an active order exists, false otherwise
     */
    boolean getActiveOrderForBook(Long bookId, Long userId);

//    /**
//     * Returns an order by its ID.
//     *
//     * @param id the ID of the order
//     * @return optional containing the order or empty
//     */
//    Optional<Order> getOrderById(Long id);

    /**
     * Returns the status of a copy if it is currently issued or reserved.
     *
     * @param copyId the ID of the book copy
     * @return status string or null
     */
    Optional<String> getIssuedOrReserved(Long copyId);

    /**
     * Returns all orders placed by the specified user.
     *
     * @param username the username of the user
     * @return list of orders
     */
    List<Order> getOrdersByUsername(String username);

    /**
     * Returns all orders in the system.
     *
     * @return list of all orders
     */
    List<Order> getAllOrders();

    /**
     * Returns the most requested books, ordered by number of requests.
     *
     * @param limit maximum number of books to return
     * @return list of Object arrays: [bookId, title, firstName, lastName, genre, requestCount]
     */
    List<BookStatsDto> getTopRequestedBooks(int limit);

    /**
     * Returns users with the highest number of completed orders.
     *
     * @param limit maximum number of users to return
     * @return list of Object arrays: [userId, fullName, email, requestCount]
     */
    List<UserStatsDto> getTopActiveUsers(int limit);
}
