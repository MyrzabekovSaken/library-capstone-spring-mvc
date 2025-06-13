package com.library.app.model;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ResourceBundle;

/**
 * Enum representing the type of book order.
 */
public enum OrderType {
    /**
     * The book is requested for reading inside the library.
     */
    READING_ROOM,
    /**
     * The book is requested for home use.
     */
    HOME;

    /**
     * Returns a user-friendly string representation of the order type.
     *
     * @return formatted order type
     */
    @Override
    public String toString() {
        return switch (this) {
            case READING_ROOM -> "READING ROOM";
            case HOME -> name();
        };
    }
}
