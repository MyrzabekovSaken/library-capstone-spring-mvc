package com.library.app.dao;

import com.library.app.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for performing CRUD and query operations on books.
 */
public interface BookDao {
    /**
     * Saves a new book to the database.
     *
     * @param book the book to save
     */
    void save(Book book);

    /**
     * Updates an existing book.
     *
     * @param book the book to update
     */
    void update(Book book);

    /**
     * Deletes a book by its ID.
     *
     * @param id the book ID
     */
    void delete(Long id);

    /**
     * Counts the total number of books.
     *
     * @return total number of books
     */
    long count();

    /**
     * Finds a book by its ID.
     *
     * @param id the book ID
     * @return optional containing the book
     */
    Optional<Book> findById(Long id);

    /**
     * Searches for books based on specified fields.
     *
     * @param title  the title to search for (nullable)
     * @param author the author to search for (nullable)
     * @param genre  the genre to search for (nullable)
     * @return list of matching books
     */
    List<Book> search(String title, String author, String genre);
}