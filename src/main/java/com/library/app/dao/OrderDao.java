package com.library.app.dao;

import com.library.app.model.Order;
import com.library.app.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for performing operations on book orders.
 */
public interface OrderDao {
    /**
     * Saves a new order.
     *
     * @param order the order to save
     */
    void save(Order order);

    /**
     * Updates an existing order.
     *
     * @param order the order to update
     */
    void update(Order order);

    /**
     * Counts the number of orders with the specified statuses.
     *
     * @param statuses list of order statuses
     * @return count of orders
     */
    long countOrderStatus(List<OrderStatus> statuses);

    /**
     * Checks if a user has an active order for a specific book.
     *
     * @param bookId the book ID
     * @param userId the user ID
     * @return true if an active order exists, false otherwise
     */
    boolean hasActiveOrderForBook(Long bookId, Long userId);

    /**
     * Finds an order by its ID.
     *
     * @param id the order ID
     * @return optional containing the order
     */
    Optional<Order> findById(Long id);

    /**
     * Checks if a book copy is issued or reserved.
     *
     * @param copyId the copy ID
     * @return optional status string
     */
    Optional<String> findIssuedOrReserved(Long copyId);

    /**
     * Finds all orders for a specific username.
     *
     * @param username the username
     * @return list of orders
     */
    List<Order> findByUsername(String username);

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all orders
     */
    List<Order> findAllOrders();

    /**
     * Returns a list of top requested books by number of orders.
     *
     * @param limit the maximum number of results
     * @return list of object arrays [Book, requestCount]
     */
    List<Object[]> findTopRequestedBooks(int limit);

    /**
     * Returns a list of top users who made the most orders.
     *
     * @param limit the maximum number of users
     * @return list of object arrays [User, orderCount]
     */
    List<Object[]> findTopActiveUsers(int limit);
}
