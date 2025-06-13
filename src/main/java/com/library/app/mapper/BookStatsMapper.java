package com.library.app.mapper;

import com.library.app.dto.BookStatsDto;

/**
 * Utility class for mapping book statistics data.
 */
public class BookStatsMapper {
    /**
     * Converts a row of data into a {@link BookStatsDto}.
     *
     * @param row row an array containing book statistics data. Expected format:
     *            index 1 - book title (String),
     *            index 2 - author's first name (String),
     *            index 3 - author's last name (String),
     *            index 5 - request count (Number).
     * @return a {@link BookStatsDto} object containing mapped data.
     */
    public static BookStatsDto toDto(Object[] row) {
        BookStatsDto bookStatsDto = new BookStatsDto();
        bookStatsDto.setTitle((String) row[1]);
        bookStatsDto.setAuthorFullName(row[2] + " " + row[3]);
        bookStatsDto.setRequestCount(((Number) row[5]).longValue());

        return bookStatsDto;
    }
}
