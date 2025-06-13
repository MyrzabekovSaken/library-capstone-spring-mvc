package com.library.app.service;

import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing book copies (inventory).
 */
public interface BookCopyService {
    /**
     * Returns the number of available (not issued) copies for the specified book.
     *
     * @param bookId the ID of the book
     * @return number of available copies
     */
    int getAvailableCopiesCount(Long bookId);

    /**
     * Saves a new book copy.
     *
     * @param copy the book copy to save
     */
    void saveBook(BookCopy copy);

    /**
     * Deletes the specified book copy.
     *
     * @param copyId the ID of the copy to delete
     */
    void deleteBook(Long copyId);

    /**
     * Updates the specified book copy.
     *
     * @param copy the book copy with updated information
     */
    void update(BookCopy copy);

    /**
     * Returns the total number of book copies.
     *
     * @return total count of copies
     */
    long countAll();

    /**
     * Returns the number of book copies with the specified status.
     *
     * @param status the status to count
     * @return number of copies with the given status
     */
    long countByStatus(CopyStatus status);

    /**
     * Generates the next inventory number for a new copy of the specified book.
     *
     * @param bookId the ID of the book
     * @return the next inventory number as a string
     */
    String generateNextInventoryNumber(Long bookId);

    /**
     * Returns a book copy by its ID.
     *
     * @param copyId the ID of the copy
     * @return optional containing the found book copy or empty if not found
     */
    Optional<BookCopy> getById(Long copyId);

    /**
     * Returns all copies associated with the specified book ID.
     *
     * @param id the book ID
     * @return list of book copies
     */
    List<BookCopy> getAllByBookId(Long id);
}
