package com.library.app.model;

/**
 * Enum representing the status of a book order.
 */
public enum OrderStatus {
    /**
     * The order has been created but not yet issued.
     */
    PENDING,
    /**
     * The book has been issued to the user.
     */
    ISSUED,
    /**
     * The book has been returned by the user.
     */
    RETURNED,
    /**
     * The order has been canceled.
     */
    CANCELED
}
