package com.library.app.dto;

/**
 * DTO for transferring book data to the presentation layer.
 */
public class BookDto {
    /**
     * The unique identifier for the book.
     */
    private Long id;
    /**
     * The title of the book.
     */
    private String title;
    /**
     * The first name of the author.
     */
    private String authorFirstName;
    /**
     * The last name of the author.
     */
    private String authorLastName;
    /**
     * The genre of the book.
     */
    private String genre;
    /**
     * A brief description of the book.
     */
    private String description;
    /**
     * The URL to the book's cover image.
     */
    private String coverUrl;

    /**
     * Default constructor
     */
    public BookDto() {
    }

    /**
     * Constructs a new {@code BookDto} instance with the specified values.
     *
     * @param id              the unique ID of the book
     * @param title           the title of the book
     * @param authorFirstName the first name of the author
     * @param authorLastName  the last name of the author
     * @param genre           the genre of the book
     * @param description     the book's description
     * @param coverUrl        the URL to the book's cover image
     */
    public BookDto(Long id, String title, String authorFirstName, String authorLastName, String genre,
                   String description, String coverUrl) {
        this.id = id;
        this.title = title;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.genre = genre;
        this.description = description;
        this.coverUrl = coverUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
