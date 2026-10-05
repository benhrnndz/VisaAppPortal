package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: User
 *  OOP CONCEPT: Encapsulation
 * ============================================================
 *
 * Represents an authenticated user profile with system role.
 */
public class User {
    private int id;
    private String email;
    private String password;
    private String role; // "APPLICANT" or "ADMIN"

    public User(int id, String email, String password, String role) {
        this.id = id;
        this.email = email != null ? email.trim() : "";
        this.password = password != null ? password : "";
        this.role = role != null ? role.trim().toUpperCase() : "APPLICANT";
    }

    public User(String email, String password, String role) {
        this(-1, email, password, role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim().toUpperCase() : "APPLICANT";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User #" + id + " [" + email + "] (" + role + ")";
    }
}
