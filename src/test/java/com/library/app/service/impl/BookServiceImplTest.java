package com.library.app.service.impl;

import com.library.app.dao.BookDao;
import com.library.app.dto.BookDto;
import com.library.app.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    // Константы
    private static final Long BOOK_ID = 1L;
    private static final Long TEN_LONG = 10L;
    private static final String TITLE = "Test Book";
    private static final String AUTHOR_FIRST_NAME = "John";
    private static final String AUTHOR_LAST_NAME = "Doe";
    private static final String GENRE = "Drama";
    public static final String DB_ERROR = "DB error";

    // Моки
    @Mock
    private BookDao bookDao;

    // Инжект мокс
    @InjectMocks
    private BookServiceImpl testingInstance;

    // Тесты
    // POSITIVE TESTS

    @Test
    void shouldSearchBook() {
        // Given
        Book book = getBook();
        List<Book> books = List.of(book);
        when(bookDao.search(TITLE, AUTHOR_FIRST_NAME, GENRE)).thenReturn(books);
        // When
        List<BookDto> result = testingInstance.search(TITLE, AUTHOR_FIRST_NAME, GENRE);
        // Then
        verify(bookDao).search(TITLE, AUTHOR_FIRST_NAME, GENRE);
        assertEquals(1, result.size());
        assertEquals(BOOK_ID, result.get(0).getId());
    }

    @Test
    void shouldGetBookById() {
        // Given
        Book book = getBook();
        when(bookDao.findById(BOOK_ID)).thenReturn(Optional.of(book));
        // When
        Optional<BookDto> result = testingInstance.getById(BOOK_ID);
        // Then
        verify(bookDao).findById(BOOK_ID);
        assertTrue(result.isPresent());
        assertEquals(BOOK_ID, result.get().getId());
    }

    @Test
    void shouldSaveNewBook() {
        // Given
        BookDto dto = new BookDto();
        dto.setTitle(TITLE);
        // When
        testingInstance.saveBook(dto);
        // Then
        verify(bookDao).save(any(Book.class));
    }

    @Test
    void shouldUpdateBook() {
        // Given
        BookDto dto = new BookDto();
        dto.setTitle(TITLE);
        // When
        testingInstance.updateBook(dto);
        // Then
        verify(bookDao).update(any(Book.class));
    }

    @Test
    void shouldDeleteBook() {
        // When
        testingInstance.deleteBook(BOOK_ID);
        // Then
        verify(bookDao).delete(BOOK_ID);
    }

    @Test
    void shouldCountBooks() {
        // Given
        when(bookDao.count()).thenReturn(TEN_LONG);
        // When
        long result = testingInstance.countBooks();
        // Then
        verify(bookDao).count();
        assertEquals(TEN_LONG, result);
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotGetBookByIdWhenNotFound() {
        // Given
        when(bookDao.findById(BOOK_ID)).thenReturn(Optional.empty());
        // When
        Optional<BookDto> result = testingInstance.getById(BOOK_ID);
        // Then
        verify(bookDao).findById(BOOK_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotSaveNewBookWhenIsNull() {
        // Then
        assertThrows(NullPointerException.class, () -> testingInstance.saveBook(null));
    }

    @Test
    void shouldNotUpdateBookWhenIsNull() {
        // Then
        assertThrows(NullPointerException.class, () -> testingInstance.updateBook(null));
    }

    @Test
    void shouldNotsaveBookWhenDaoFails() {
        // Given
        BookDto dto = new BookDto();
        dto.setTitle(TITLE);
        doThrow(new RuntimeException(DB_ERROR)).when(bookDao).save(any(Book.class));
        // Then
        verify(bookDao).save(any(Book.class));
        assertThrows(RuntimeException.class, () -> testingInstance.saveBook(dto));
    }

    @Test
    void shouldNotUpdateBookWhenDaoFails() {
        // Given
        BookDto dto = new BookDto();
        dto.setTitle(TITLE);
        doThrow(new RuntimeException(DB_ERROR)).when(bookDao).update(any(Book.class));
        // Then
        verify(bookDao).update(any(Book.class));
        assertThrows(RuntimeException.class, () -> testingInstance.updateBook(dto));
    }

    @Test
    void shouldNotSearchWhenNoMatches() {
        // Given
        when(bookDao.search(TITLE, AUTHOR_FIRST_NAME, GENRE)).thenReturn(Collections.emptyList());
        // When
        List<BookDto> result = testingInstance.search(TITLE, AUTHOR_FIRST_NAME, GENRE);
        // Then
        verify(bookDao).search(TITLE, AUTHOR_FIRST_NAME, GENRE);
        assertTrue(result.isEmpty());
    }

    private static Book getBook() {
        Book book = new Book();
        book.setId(BOOK_ID);
        book.setTitle(TITLE);
        book.setAuthorFirstName(AUTHOR_FIRST_NAME);
        book.setAuthorLastName(AUTHOR_LAST_NAME);
        book.setGenre(GENRE);

        return book;
    }
}
