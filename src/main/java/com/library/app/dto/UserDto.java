package com.library.app.dto;

import com.library.app.model.Role;

/**
 * DTO for transferring user data to the presentation layer.
 */
public class UserDto {
    /**
     * The unique identifier for the user.
     */
    private Long id;
    /**
     * The username of the user.
     */
    private String username;
    /**
     * The email address of the user.
     */
    private String email;
    /**
     * The current status of the user.
     */
    private String status;
    /**
     * The role assigned to the user (e.g., ADMIN, LIBRARIAN).
     */
    private Role role;

    public UserDto() {
    }

    /**
     * Constructs a new {@code UserDto} instance with the specified values.
     *
     * @param id       the unique ID of the user
     * @param username the username of the user
     * @param email    the email address of the user
     * @param status   the current status of the user
     * @param role     the role assigned to the user
     */
    public UserDto(Long id, String username, String email, String status, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
