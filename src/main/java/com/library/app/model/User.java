package com.library.app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a user in the system.
 * Can be a reader, librarian, or administrator.
 */
public class User {
    /**
     * The unique identifier for the user.
     */
    private Long id;
    /**
     * The username of the user.
     * Cannot be blank.
     */
    @NotBlank(message = "Username is required")
    private String username;
    /**
     * The email address of the user.
     * Must be a valid email format and cannot be blank.
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    /**
     * The password for the user account.
     * Cannot be blank.
     */
    @NotBlank(message = "Password is required")
    private String password;
    /**
     * The current status of the user.
     */
    private String status;
    /**
     * The role assigned to the user (e.g., ADMIN, LIBRARIAN).
     */
    private Role role;

    public User() {
    }

    /**
     * Constructs a fully initialized {@code User} instance.
     *
     * @param id       the unique identifier of the user
     * @param username the login name of the user
     * @param email    the email address of the user
     * @param password the hashed password
     * @param status   the status of the account (e.g. ACTIVE, BLOCKED)
     * @param role     the role of the user (e.g. READER, LIBRARIAN, ADMIN)
     */
    public User(Long id, String username, String email, String password, String status, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
