package com.library.app.model;

/**
 * Represents a physical copy of a book, identified by an inventory number.
 * Each copy has its own status (e.g., AVAILABLE, ISSUED).
 */
public class BookCopy {
    /**
     * The unique identifier of this book copy.
     */
    private Long id;
    /**
     * The inventory number assigned to this book copy.
     */
    private String inventoryNumber;
    /**
     * The book to which this copy belongs.
     */
    private Book book;
    /**
     * The current status of this book copy (e.g., AVAILABLE, ISSUED).
     */
    private CopyStatus status;

    /**
     * Default constructor
     */
    public BookCopy() {
    }

    /**
     * Constructs a {@code BookCopy} with the specified values.
     *
     * @param id              the ID of the copy
     * @param inventoryNumber the unique inventory number
     * @param book            the book to which this copy belongs
     * @param status          the current status of the copy
     */
    public BookCopy(Long id, String inventoryNumber, Book book, CopyStatus status) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.book = book;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }
}
