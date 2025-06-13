package com.library.app.model;

import java.time.LocalDate;

/**
 * Represents an order placed by a user for a specific book copy.
 * Contains information about order type, status, and key dates.
 */
public class Order {
    /**
     * The unique identifier of the order.
     */
    private Long id;
    /**
     * The user who placed the order.
     */
    private User user;
    /**
     * The specific copy of the book being ordered.
     */
    private BookCopy bookCopy;
    /**
     * The type of the order (e.g., HOME, READING_ROOM).
     */
    private OrderType type;
    /**
     * The current status of the order.
     */
    private OrderStatus status;
    /**
     * The date the book was issued (if applicable).
     */
    private LocalDate issueDate;
    /**
     * The date the book is due to be returned.
     */
    private LocalDate dueDate;
    /**
     * The date the book was actually returned.
     */
    private LocalDate returnDate;

    public Order() {
    }

    /**
     * Constructs a fully initialized {@code Order} instance.
     *
     * @param id         the unique ID of the order
     * @param user       the user who placed the order
     * @param bookCopy   the specific copy of the book being ordered
     * @param type       the type of the order (e.g. HOME, READING_ROOM)
     * @param status     the current status of the order
     * @param issueDate  the date the book was issued
     * @param dueDate    the date the book is due to be returned
     * @param returnDate the date the book was actually returned
     */
    public Order(Long id, User user, BookCopy bookCopy, OrderType type,
                 OrderStatus status, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate) {
        this.id = id;
        this.user = user;
        this.bookCopy = bookCopy;
        this.type = type;
        this.status = status;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
