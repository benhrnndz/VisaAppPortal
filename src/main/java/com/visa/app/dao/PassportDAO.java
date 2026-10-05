package com.visa.app.dao;

import com.visa.app.model.Passport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for `passports` table.
 */
public class PassportDAO {

    private static final Logger LOGGER = Logger.getLogger(PassportDAO.class.getName());

    public boolean insertOrUpdatePassport(Passport passport) {
        if (passport == null || passport.getPassportNo().isBlank()) {
            return false;
        }
        String sql = "INSERT OR REPLACE INTO passports (passport_no, issued_by, date_of_issue, valid_until) " +
                     "VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, passport.getPassportNo());
                ps.setString(2, passport.getIssuedBy().isBlank() ? "Unknown" : passport.getIssuedBy());
                ps.setString(3, passport.getDateOfIssue().isBlank() ? "2020/01/01" : passport.getDateOfIssue());
                ps.setString(4, passport.getValidUntil().isBlank() ? "2030/01/01" : passport.getValidUntil());
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving passport: " + passport.getPassportNo(), e);
            return false;
        }
    }

    public Passport getPassportByNumber(String passportNo) {
        if (passportNo == null || passportNo.isBlank()) {
            return null;
        }
        String sql = "SELECT passport_no, issued_by, date_of_issue, valid_until FROM passports WHERE passport_no = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, passportNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Passport(
                                rs.getString("passport_no"),
                                rs.getString("issued_by"),
                                rs.getString("date_of_issue"),
                                rs.getString("valid_until")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching passport: " + passportNo, e);
        }
        return null;
    }
}
