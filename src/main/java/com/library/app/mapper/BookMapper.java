package com.library.app.mapper;

import com.library.app.dto.BookDto;
import com.library.app.model.Book;

/**
 * Utility class for mapping between {@code Book} entities and {@code BookDto} objects.
 */
public class BookMapper {
    /**
     * Converts a {@code Book} entity into a {@code BookDto}.
     *
     * @param book the book entity to convert
     * @return the corresponding {@code BookDto} object
     */
    public static BookDto toDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthorFirstName(book.getAuthorFirstName());
        bookDto.setAuthorLastName(book.getAuthorLastName());
        bookDto.setGenre(book.getGenre());
        bookDto.setDescription(book.getDescription());
        bookDto.setCoverUrl(book.getCoverUrl());

        return bookDto;
    }

    /**
     * Converts a {@code BookDto} into a {@code Book} entity.
     *
     * @param bookDto the book DTO to convert
     * @return the corresponding {@code Book} entity
     */
    public static Book toEntity(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthorFirstName(bookDto.getAuthorFirstName());
        book.setAuthorLastName(bookDto.getAuthorLastName());
        book.setGenre(bookDto.getGenre());
        book.setDescription(bookDto.getDescription());
        book.setCoverUrl(bookDto.getCoverUrl());

        return book;
    }
}