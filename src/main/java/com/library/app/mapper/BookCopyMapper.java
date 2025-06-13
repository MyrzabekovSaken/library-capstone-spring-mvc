package com.library.app.mapper;

import com.library.app.dto.BookCopyDto;
import com.library.app.model.Book;
import com.library.app.model.BookCopy;

/**
 * Utility class for mapping between {@code BookCopy} entities and {@code BookCopyDto} objects.
 */
public class BookCopyMapper {
    /**
     * Converts a {@code BookCopy} entity into a {@code BookCopyDto}.
     *
     * @param copy the book copy entity to convert
     * @return the corresponding {@code BookCopyDto} object, or {@code null} if input is null
     */
    public static BookCopyDto toDto(BookCopy copy) {
        if (copy != null) {
            return new BookCopyDto(
                    copy.getId(),
                    copy.getInventoryNumber(),
                    copy.getBook() != null ? copy.getBook().getId() : null,
                    copy.getBook() != null ? copy.getBook().getTitle() : null,
                    copy.getStatus() != null ? copy.getStatus().toString() : null
            );
        }

        return null;
    }

    /**
     * Converts a {@code BookCopyDto} into a {@code BookCopy} entity.
     *
     * @param bookCopyDto the book copy DTO to convert
     * @param book        the associated book entity
     * @return the corresponding {@code BookCopy} entity, or {@code null} if input DTO is null
     */
    public static BookCopy toEntity(BookCopyDto bookCopyDto, Book book) {
        if (bookCopyDto != null) {
            BookCopy copy = new BookCopy();
            copy.setId(bookCopyDto.getId());
            copy.setInventoryNumber(bookCopyDto.getInventoryNumber());
            copy.setBook(book);
            copy.setStatus(bookCopyDto.getStatus() != null
                    ? Enum.valueOf(com.library.app.model.CopyStatus.class, bookCopyDto.getStatus()) : null);

            return copy;
        }

        return null;
    }
}
