package com.library.app.dao;

import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for performing CRUD and query operations on book copies.
 */
public interface BookCopyDao {
    /**
     * Saves a new book copy to the database.
     *
     * @param copy the book copy to save
     */
    void save(BookCopy copy);

    /**
     * Updates an existing book copy in the database.
     *
     * @param copy the book copy to update
     */
    void update(BookCopy copy);

    /**
     * Deletes a book copy by its ID.
     *
     * @param id the ID of the book copy
     */
    void delete(Long id);

    /**
     * Returns the number of available copies for a specific book.
     *
     * @param bookId the book ID
     * @return count of available copies
     */
    int countAvailableCopies(Long bookId);

    /**
     * Counts the total number of book copies.
     *
     * @return total count of book copies
     */
    long countAllBookCopy();

    /**
     * Counts the number of book copies with a specific status.
     *
     * @param status the copy status (e.g., AVAILABLE, RESERVED)
     * @return count of book copies with given status
     */
    long countBookCopyStatus(CopyStatus status);

    /**
     * Finds an available copy of a book.
     *
     * @param bookId the book ID
     * @return optional containing available copy if found
     */
    Optional<BookCopy> findAvailableCopy(Long bookId);

    /**
     * Retrieves the last used inventory number for a book.
     *
     * @param bookId the book ID
     * @return optional containing the last inventory number
     */
    Optional<String> findLastInventoryNumber(Long bookId);

    /**
     * Retrieves a book copy by its ID.
     *
     * @param id the ID of the copy
     * @return optional containing the book copy
     */
    Optional<BookCopy> findById(Long id);

    /**
     * Retrieves all copies for a specific book.
     *
     * @param bookId the book ID
     * @return list of book copies
     */
    List<BookCopy> findAllByBookId(Long bookId);
}
