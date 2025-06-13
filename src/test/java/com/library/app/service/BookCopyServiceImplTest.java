package com.library.app.service;

import com.library.app.dao.BookCopyDao;
import com.library.app.model.BookCopy;
import com.library.app.model.CopyStatus;
import com.library.app.service.impl.BookCopyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCopyServiceImplTest {
    // Константы
    private static final Long BOOK_ID = 1L;
    private static final Long COPY_ID = 10L;
    private static final Long FIVE_LONG = 5L;
    private static final Long FORTY_TWO_LONG = 42L;
    private static final String LAST_INVENTORY = "INV-0007";
    private static final String INVALID = "INVALID";
    private static final String INV_0001 = "INV-0001";
    private static final String INV_0008 = "INV-0008";
    public static final String DB_ERROR = "DB error";

    // Моки
    @Mock
    private BookCopyDao bookCopyDao;

    // Инжект мокс
    @InjectMocks
    private BookCopyServiceImpl testingInstance;

    // Тесты
    // POSITIVE TESTS

    @Test
    void shouldGetAvailableCopiesCount() {
        // Given
        when(bookCopyDao.countAvailableCopies(BOOK_ID)).thenReturn(3);
        // When
        int result = testingInstance.getAvailableCopiesCount(BOOK_ID);
        // Then
        assertEquals(3, result);
        verify(bookCopyDao).countAvailableCopies(BOOK_ID);
    }

    @Test
    void shouldGetAllByBookId() {
        // Given
        List<BookCopy> expected = List.of(getCopy());
        when(bookCopyDao.findAllByBookId(BOOK_ID)).thenReturn(expected);
        // When
        List<BookCopy> actual = testingInstance.getAllByBookId(BOOK_ID);
        // Then
        assertEquals(expected, actual);
        verify(bookCopyDao).findAllByBookId(BOOK_ID);
    }

    @Test
    void shouldGetCopyById() {
        // Given
        BookCopy copy = getCopy();
        when(bookCopyDao.findById(COPY_ID)).thenReturn(Optional.of(copy));
        // When
        Optional<BookCopy> result = testingInstance.getById(COPY_ID);
        // Then
        assertTrue(result.isPresent());
        assertEquals(copy, result.get());
        verify(bookCopyDao).findById(COPY_ID);
    }

    @Test
    void shouldGenerateNextInventoryNumber() {
        // Given
        when(bookCopyDao.findLastInventoryNumber(BOOK_ID)).thenReturn(Optional.of(LAST_INVENTORY));
        // When
        String result = testingInstance.generateNextInventoryNumber(BOOK_ID);
        // Then
        assertEquals(INV_0008, result);
        verify(bookCopyDao).findLastInventoryNumber(BOOK_ID);
    }

    @Test
    void shouldGenerateNextInventoryNumberNoneExists() {
        // Given
        when(bookCopyDao.findLastInventoryNumber(BOOK_ID)).thenReturn(Optional.empty());
        // When
        String result = testingInstance.generateNextInventoryNumber(BOOK_ID);
        // Then
        assertEquals(INV_0001, result);
        verify(bookCopyDao).findLastInventoryNumber(BOOK_ID);
    }

    @Test
    void shouldCountAllCopy() {
        // Given
        when(bookCopyDao.countAllBookCopy()).thenReturn(FORTY_TWO_LONG);
        // When
        long result = testingInstance.countAll();
        // Then
        assertEquals(FORTY_TWO_LONG, result);
        verify(bookCopyDao).countAllBookCopy();
    }

    @Test
    void shouldCountCopyByStatus() {
        // Given
        when(bookCopyDao.countBookCopyStatus(CopyStatus.ISSUED)).thenReturn(FIVE_LONG);
        // When
        long result = testingInstance.countByStatus(CopyStatus.ISSUED);
        // Then
        assertEquals(FIVE_LONG, result);
        verify(bookCopyDao).countBookCopyStatus(CopyStatus.ISSUED);
    }

    @Test
    void shouldSaveNewCopyBook() {
        // Given
        BookCopy copy = getCopy();
        // When
        testingInstance.saveBook(copy);
        // Then
        verify(bookCopyDao).save(copy);
    }

    @Test
    void shouldDeleteCopyBook() {
        // When
        testingInstance.deleteBook(COPY_ID);
        // Then
        verify(bookCopyDao).delete(COPY_ID);
    }

    @Test
    void shouldUpdateCopyBook() {
        // Given
        BookCopy copy = getCopy();
        // When
        testingInstance.update(copy);
        // Then
        verify(bookCopyDao).update(copy);
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotGetCopyByIdWhenNotExists() {
        // Given
        when(bookCopyDao.findById(COPY_ID)).thenReturn(Optional.empty());
        // When
        Optional<BookCopy> result = testingInstance.getById(COPY_ID);
        // Then
        assertTrue(result.isEmpty());
        verify(bookCopyDao).findById(COPY_ID);
    }

    @Test
    void shouldNotGenerateNextInventoryNumberWhenInvalidFormat() {
        // Given
        when(bookCopyDao.findLastInventoryNumber(BOOK_ID)).thenReturn(Optional.of(INVALID));
        // When
        String result = testingInstance.generateNextInventoryNumber(BOOK_ID);
        // Then
        assertEquals(INV_0001, result);
        verify(bookCopyDao).findLastInventoryNumber(BOOK_ID);
    }

    @Test
    void shouldNotSaveBookWhenThrowsException() {
        // Given
        BookCopy copy = getCopy();
        doThrow(new RuntimeException(DB_ERROR)).when(bookCopyDao).save(copy);
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.saveBook(copy));
        verify(bookCopyDao).save(copy);
    }

    @Test
    void shouldNotDeleteBookWhenThrowsException() {
        // Given
        doThrow(new RuntimeException(DB_ERROR)).when(bookCopyDao).delete(COPY_ID);
        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.deleteBook(COPY_ID));
        verify(bookCopyDao).delete(COPY_ID);
    }

    @Test
    void shouldNotUpdateWhenThrowsException() {
        // Given
        BookCopy copy = getCopy();
        doThrow(new RuntimeException(DB_ERROR)).when(bookCopyDao).update(copy);

        // Then
        assertThrows(RuntimeException.class, () -> testingInstance.update(copy));
        verify(bookCopyDao).update(copy);
    }

    @Test
    void shouldNotSaveBookWhenCopyIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> testingInstance.saveBook(null));
    }

    @Test
    void shouldNotUpdateWhenCopyIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> testingInstance.update(null));
    }

    private static BookCopy getCopy() {
        BookCopy copy = new BookCopy();
        copy.setId(COPY_ID);
        copy.setInventoryNumber(INV_0001);

        return copy;
    }
}
