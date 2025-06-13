package com.library.app.service.impl;

import com.library.app.dao.BookDao;
import com.library.app.dto.BookDto;
import com.library.app.mapper.BookMapper;
import com.library.app.model.Book;
import com.library.app.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing books.
 */
@Service
public class BookServiceImpl implements BookService {
    private final BookDao bookDao;

    /**
     * Constructs a {@code BookServiceImpl} with the specified {@code BookDao}.
     *
     * @param bookDao the DAO responsible for managing book operations
     */
    @Autowired
    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    /**
     * Searches for books by optional filters: title, author, genre.
     *
     * @param title  book title (nullable)
     * @param author book author (nullable)
     * @param genre  book genre (nullable)
     * @return list of matching books
     */
    @Override
    public List<BookDto> search(String title, String author, String genre) {
        return bookDao.search(title, author, genre).stream()
                .map(BookMapper::toDto)
                .toList();
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param id the book ID
     * @return optional containing the book if found
     */
    @Override
    public Optional<BookDto> getById(Long id) {
        return bookDao.findById(id).map(BookMapper::toDto);
    }

    /**
     * Saves a new book.
     *
     * @param bookDto the book to save
     */
    @Override
    public void saveBook(BookDto bookDto) {
        Book book = BookMapper.toEntity(bookDto);
        bookDao.save(book);
    }

    /**
     * Updates an existing book.
     *
     * @param bookDto the updated book data
     */
    @Override
    public void updateBook(BookDto bookDto) {
        Book book = BookMapper.toEntity(bookDto);
        bookDao.update(book);
    }

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     */
    @Override
    public void deleteBook(Long id) {
        bookDao.delete(id);
    }

    /**
     * Returns the total number of books in the catalog.
     *
     * @return number of books
     */
    @Override
    public long countBooks() {
        return bookDao.count();
    }

}
