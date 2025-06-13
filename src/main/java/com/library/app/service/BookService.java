package com.library.app.service;

import com.library.app.dto.BookDto;
import com.library.app.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing books.
 */
public interface BookService {
    /**
     * Saves a new book.
     *
     * @param bookDto the book to save
     */
    void saveBook(BookDto bookDto);

    /**
     * Updates an existing book.
     *
     * @param bookDto the updated book data
     */
    void updateBook(BookDto bookDto);

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     */
    void deleteBook(Long id);

    /**
     * Returns the total number of books.
     *
     * @return total count of books
     */
    long countBooks();

    /**
     * Returns a book by its ID.
     *
     * @param id the book ID
     * @return optional containing the found book or empty if not found
     */
    Optional<BookDto> getById(Long id);

    /**
     * Searches for books based on optional title, author, and genre.
     *
     * @param title  book title (nullable)
     * @param author book author (nullable)
     * @param genre  book genre (nullable)
     * @return list of matching books
     */
    List<BookDto> search(String title, String author, String genre);
}
