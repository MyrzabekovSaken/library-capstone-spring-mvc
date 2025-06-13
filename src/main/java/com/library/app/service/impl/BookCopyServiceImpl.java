package com.library.app.service.impl;

import com.library.app.dao.BookCopyDao;
import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;
import com.library.app.service.BookCopyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service implementation for managing book copies.
 */
@Service
public class BookCopyServiceImpl implements BookCopyService {
    private static final Logger logger = LoggerFactory.getLogger(BookCopyServiceImpl.class);
    private static final String INVALID_INVENTORY_NUMBER_FORMAT = "Invalid inventory number format: {}";
    private static final String INV_04_D = "INV-%04d";
    private static final String BOOK_COPY_MUST_NOT_BE_NULL = "BookCopy must not be null";

    private final BookCopyDao bookCopyDao;

    /**
     * Constructs a {@code BookCopyServiceImpl} with the specified data access object.
     *
     * @param bookCopyDao the DAO responsible for managing book copies
     */
    @Autowired
    public BookCopyServiceImpl(BookCopyDao bookCopyDao) {
        this.bookCopyDao = bookCopyDao;
    }

    /**
     * Returns the number of available book copies for a given book.
     *
     * @param bookId the ID of the book
     * @return number of available copies
     */
    @Override
    public int getAvailableCopiesCount(Long bookId) {
        return bookCopyDao.countAvailableCopies(bookId);
    }

    /**
     * Retrieves all copies of a given book by its ID.
     *
     * @param id the book ID
     * @return list of book copies
     */
    @Override
    public List<BookCopy> getAllByBookId(Long id) {
        return bookCopyDao.findAllByBookId(id);
    }

    /**
     * Saves a new book copy.
     *
     * @param copy the book copy to save
     */
    @Override
    public void saveBook(BookCopy copy) {
        Objects.requireNonNull(copy, BOOK_COPY_MUST_NOT_BE_NULL);
        bookCopyDao.save(copy);
    }

    /**
     * Deletes a book copy by its ID.
     *
     * @param copyId the ID of the copy to delete
     */
    @Override
    public void deleteBook(Long copyId) {
        bookCopyDao.delete(copyId);
    }

    /**
     * Updates a book copy.
     *
     * @param copy the book copy with updated information
     */
    @Override
    public void update(BookCopy copy) {
        Objects.requireNonNull(copy, BOOK_COPY_MUST_NOT_BE_NULL);
        bookCopyDao.update(copy);
    }

    /**
     * Retrieves a book copy by its ID.
     *
     * @param copyId the ID of the copy
     * @return optional containing the book copy if found
     */
    @Override
    public Optional<BookCopy> getById(Long copyId) {
        return bookCopyDao.findById(copyId);
    }

    /**
     * Generates the next inventory number for a given book.
     *
     * @param bookId the ID of the book
     * @return the generated inventory number (e.g., "INV-0002")
     */
    @Override
    public String generateNextInventoryNumber(Long bookId) {
        Optional<String> last = bookCopyDao.findLastInventoryNumber(bookId);

        int nextNumber = 1;
        if (last.isPresent()) {
            String lastInv = last.get();
            try {
                String numberPart = lastInv.substring(4);
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                logger.warn(INVALID_INVENTORY_NUMBER_FORMAT, lastInv, e);
            }
        }

        return String.format(INV_04_D, nextNumber);
    }

    /**
     * Returns the total number of book copies in the system.
     *
     * @return total count
     */
    @Override
    public long countAll() {
        return bookCopyDao.countAllBookCopy();
    }

    /**
     * Returns the count of book copies with a specific status.
     *
     * @param status the copy status
     * @return count by status
     */
    @Override
    public long countByStatus(CopyStatus status) {
        return bookCopyDao.countBookCopyStatus(status);
    }
}
