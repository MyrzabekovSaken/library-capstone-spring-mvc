package com.library.app.model;

/**
 * Enum representing the status of a specific book copy in the library.
 */
public enum CopyStatus {
    /**
     * The copy is available for request.
     */
    AVAILABLE,
    /**
     * The copy is reserved by a user.
     */
    RESERVED,
    /**
     * The copy has been issued to a reader.
     */
    ISSUED,
    /**
     * The copy has been marked as lost.
     */
    LOST,
    /**
     * The copy is written off and no longer in circulation.
     */
    WRITTEN_OFF;

    /**
     * Returns a human-readable name for the status.
     *
     * @return formatted status name
     */
    @Override
    public String toString() {
        return switch (this) {
            case AVAILABLE, RESERVED, ISSUED, LOST -> name();
            case WRITTEN_OFF -> "WRITTEN OFF";
        };
    }
}
