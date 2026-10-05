package com.visa.app.dao;

import com.visa.app.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for `users` table.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public boolean registerUser(String email, String password, String role) {
        String sql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                pstmt.setString(2, password);
                pstmt.setString(3, role != null ? role.trim().toUpperCase() : "APPLICANT");
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Registration failed for email {0}: {1}", new Object[]{email, e.getMessage()});
            return false;
        }
    }

    public User loginUser(String email, String password) {
        String sql = "SELECT id, email, password, role FROM users WHERE LOWER(email) = ? AND password = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getString("role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Login error for email " + email, e);
        }
        return null;
    }

    public User findUserByEmail(String email) {
        String sql = "SELECT id, email, password, role FROM users WHERE LOWER(email) = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getString("role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }
        return null;
    }
}
