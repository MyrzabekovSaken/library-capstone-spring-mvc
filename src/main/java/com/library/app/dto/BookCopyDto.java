package com.library.app.dto;

/**
 * DTO for transferring book copy data to the presentation layer.
 */
public class BookCopyDto {
    /**
     * The unique identifier for the book copy.
     */
    private Long id;
    /**
     * The inventory number assigned to this book copy.
     */
    private String inventoryNumber;
    /**
     * The unique identifier of the associated book.
     */
    private Long bookId;
    /**
     * The title of the associated book.
     */
    private String bookTitle;
    /**
     * The current status of the book copy (e.g., AVAILABLE, ISSUED).
     */
    private String status;

    /**
     * Default constructor
     */
    public BookCopyDto() {
    }

    /**
     * Parameterized constructor to initialize a book copy DTO.
     *
     * @param id              Unique identifier for the book copy
     * @param inventoryNumber Inventory number of the book copy
     * @param bookId          Identifier of the associated book
     * @param bookTitle       Title of the associated book
     * @param status          Status of the book copy
     */
    public BookCopyDto(Long id, String inventoryNumber, Long bookId, String bookTitle, String status) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
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

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
