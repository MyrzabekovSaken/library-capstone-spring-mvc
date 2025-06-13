package com.library.app.util;

import java.util.List;

/**
 * Utility class for handling pagination logic.
 * Provides methods to paginate lists and calculate total pages.
 */
public class PaginationUtil {
    /**
     * Returns a sublist (page) of the provided list based on the page number and page size.
     *
     * @param items the full list of items to paginate
     * @param page  the page number (1-based index)
     * @param size  the number of items per page
     * @param <T>   the type of items in the list
     * @return a sublist representing the requested page
     */
    public static <T> List<T> paginate(List<T> items, int page, int size) {
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(fromIndex + size, items.size());

        return items.subList(fromIndex, toIndex);
    }

    /**
     * Calculates the total number of pages based on the total item count and page size.
     *
     * @param totalItems the total number of items
     * @param pageSize   the number of items per page
     * @return the total number of pages
     */
    public static int getTotalPages(int totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private PaginationUtil() {
        throw new IllegalStateException("Utility class");
    }
}
