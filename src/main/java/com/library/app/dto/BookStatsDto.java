package com.library.app.dto;

/**
 * Contains information about a book's title, author, and the number of times it has been requested
 */
public class BookStatsDto {
    /**
     * The title of the book.
     */
    private String title;
    /**
     * The full name of the author of the book.
     */
    private String authorFullName;
    /**
     * The number of times the book has been requested.
     */
    private Long requestCount;

    /**
     * Default constructor
     */
    public BookStatsDto() {
    }

    /**
     * Constructs a BookStatsDto with the specified title, author's full name, and request count.
     *
     * @param title          the title of the book
     * @param authorFullName the full name of the book's author
     * @param requestCount   the number of times the book has been requested
     */
    public BookStatsDto(String title, String authorFullName, Long requestCount) {
        this.title = title;
        this.authorFullName = authorFullName;
        this.requestCount = requestCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}
