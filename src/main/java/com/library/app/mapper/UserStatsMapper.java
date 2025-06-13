package com.library.app.mapper;

import com.library.app.dto.UserStatsDto;

/**
 * Utility class for mapping user statistics data.
 */
public class UserStatsMapper {
    /**
     * Converts a row of data into a {@link UserStatsDto}.
     *
     * @param row row an array containing user statistics data. Expected format:
     *            index 1 - username (String),
     *            index 2 - request count (Number).
     * @return a {@link UserStatsDto} object containing mapped data.
     */
    public static UserStatsDto toDto(Object[] row) {
        UserStatsDto userStatsDto = new UserStatsDto();
        userStatsDto.setUsername((String) row[1]);
        userStatsDto.setRequestCount(((Number) row[2]).longValue());

        return userStatsDto;
    }
}
