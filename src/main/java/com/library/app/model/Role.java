package com.library.app.model;

/**
 * Enum representing the roles available in the system.
 */
public enum Role {
    /**
     * Reader role - can browse books and make orders.
     */
    READER,
    /**
     * Librarian role - manages book lending and returns.
     */
    LIBRARIAN,
    /**
     * Administrator role - full access to manage users, books, and statistics.
     */
    ADMIN
}
