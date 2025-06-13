package com.library.app.dto;

/**
 * Contains information about a user's username and the number of requests they have made.
 */
public class UserStatsDto {
    /**
     * The username of the user.
     */
    private String username;
    /**
     * The number of requests made by the user.
     */
    private Long requestCount;

    public UserStatsDto() {
    }

    /**
     * Constructs a UserStatsDto with the specified username and request count.
     *
     * @param username     the username of the user
     * @param requestCount the number of requests made by the user
     */
    public UserStatsDto(String username, Long requestCount) {
        this.username = username;
        this.requestCount = requestCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}
