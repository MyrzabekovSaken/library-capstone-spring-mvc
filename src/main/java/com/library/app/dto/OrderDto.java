package com.library.app.dto;

import com.library.app.model.OrderStatus;
import com.library.app.model.OrderType;

import java.time.LocalDate;

/**
 * DTO for transferring order data to the presentation layer.
 */
public class OrderDto {
    /**
     * The unique identifier for the order.
     */
    private Long id;
    /**
     * The title of the book associated with the order.
     */
    private String bookTitle;
    /**
     * The inventory number of the book copy.
     */
    private String inventoryNumber;
    /**
     * The username of the user who placed the order.
     */
    private String username;
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
     * The due date for returning the book.
     */
    private LocalDate dueDate;
    /**
     * The date the book was actually returned.
     */
    private LocalDate returnDate;
    /**
     * The full name of the author.
     */
    private String authorFullName;

    public OrderDto() {
    }

    /**
     * Constructs an OrderDto with the specified details.
     *
     * @param id              the unique identifier of the order
     * @param bookTitle       the title of the book associated with the order
     * @param inventoryNumber the inventory number of the book
     * @param username        the username of the person who placed the order
     * @param type            the type of order
     * @param status          the current status of the order
     * @param issueDate       the date the book was issued
     * @param dueDate         the date the book is due to be returned
     * @param returnDate      the date the book was actually returned
     * @param authorFullName  the full name of the author of the book
     */
    public OrderDto(Long id, String bookTitle, String inventoryNumber, String username,
                    OrderType type, OrderStatus status, LocalDate issueDate, LocalDate dueDate,
                    LocalDate returnDate, String authorFullName) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.inventoryNumber = inventoryNumber;
        this.username = username;
        this.type = type;
        this.status = status;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.authorFullName = authorFullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }
}
