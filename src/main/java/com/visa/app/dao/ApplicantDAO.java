package com.visa.app.dao;

import com.visa.app.model.Applicant;
import com.visa.app.util.NameUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ============================================================
 *  DAO: ApplicantDAO
 *  QUERIES COVERED:
 *    Q1 (Simple)  — INSERT new applicant into users + applicants
 *    Q2 (Simple)  — SELECT all applicants (SELECT * FROM applicants)
 *    Q3 (Moderate)— JOIN applicants + users ON user_id WHERE applicant_id = ?
 * ============================================================
 */
public class ApplicantDAO {

    private static final Logger LOGGER = Logger.getLogger(ApplicantDAO.class.getName());

    /**
     * Q1: Simple Insert — Creates the user account and applicant record in a transaction.
     */
    public boolean insertApplicant(Applicant applicant, String email, String password) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, 'APPLICANT')";
        String sqlApplicant = "INSERT INTO applicants "
                + "(user_id, name, sex, citizenship, date_of_birth, place_of_birth, contact_no, "
                + " home_address, civil_status, spouse_name, occupation, employer_office_and_address, "
                + " father_name, mother_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, email.trim().toLowerCase());
                psUser.setString(2, password);
                psUser.executeUpdate();
                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No user ID generated.");
                    userId = keys.getInt(1);
                    applicant.setUserId(userId);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlApplicant, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt   (1,  userId);
                ps.setString(2,  applicant.getFullName());
                ps.setString(3,  applicant.getSex());
                ps.setString(4,  applicant.getCitizenship());
                ps.setString(5,  applicant.getDateOfBirth());
                ps.setString(6,  applicant.getPlaceOfBirth());
                ps.setString(7,  applicant.getContactNo());
                ps.setString(8,  applicant.getHomeAddress());
                ps.setString(9,  applicant.getCivilStatus());
                ps.setString(10, applicant.getSpouseName());
                ps.setString(11, applicant.getOccupation());
                ps.setString(12, applicant.getEmployerOfficeAndAddress());
                ps.setString(13, applicant.getFatherName());
                ps.setString(14, applicant.getMotherName());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        applicant.setApplicantId(keys.getInt(1));
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            LOGGER.log(Level.INFO, "[Q1-INSERT] Applicant created: {0}", applicant.getProfileSummary());
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "[Q1-INSERT] Error saving applicant: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Inserts an applicant row using an existing transaction connection.
     */
    public int insertApplicant(Applicant applicant, Connection conn) throws SQLException {
        String sql = "INSERT INTO applicants "
                + "(user_id, name, sex, citizenship, date_of_birth, place_of_birth, contact_no, "
                + " home_address, civil_status, spouse_name, occupation, employer_office_and_address, "
                + " father_name, mother_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1,  applicant.getUserId());
            ps.setString(2,  applicant.getFullName());
            ps.setString(3,  applicant.getSex());
            ps.setString(4,  applicant.getCitizenship());
            ps.setString(5,  applicant.getDateOfBirth());
            ps.setString(6,  applicant.getPlaceOfBirth());
            ps.setString(7,  applicant.getContactNo());
            ps.setString(8,  applicant.getHomeAddress());
            ps.setString(9,  applicant.getCivilStatus());
            ps.setString(10, applicant.getSpouseName());
            ps.setString(11, applicant.getOccupation());
            ps.setString(12, applicant.getEmployerOfficeAndAddress());
            ps.setString(13, applicant.getFatherName());
            ps.setString(14, applicant.getMotherName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    applicant.setApplicantId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    public boolean updateApplicant(Applicant applicant, Connection conn) throws SQLException {
        String sql = "UPDATE applicants SET name=?, sex=?, citizenship=?, date_of_birth=?, "
                + "place_of_birth=?, contact_no=?, home_address=?, civil_status=?, "
                + "spouse_name=?, occupation=?, employer_office_and_address=?, "
                + "father_name=?, mother_name=? WHERE applicant_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  applicant.getFullName());
            ps.setString(2,  applicant.getSex());
            ps.setString(3,  applicant.getCitizenship());
            ps.setString(4,  applicant.getDateOfBirth());
            ps.setString(5,  applicant.getPlaceOfBirth());
            ps.setString(6,  applicant.getContactNo());
            ps.setString(7,  applicant.getHomeAddress());
            ps.setString(8,  applicant.getCivilStatus());
            ps.setString(9,  applicant.getSpouseName());
            ps.setString(10, applicant.getOccupation());
            ps.setString(11, applicant.getEmployerOfficeAndAddress());
            ps.setString(12, applicant.getFatherName());
            ps.setString(13, applicant.getMotherName());
            ps.setInt   (14, applicant.getApplicantId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Q2: Simple Select All — loads all applicants from `applicants` table.
     */
    public List<Applicant> getAllApplicants() {
        String sql = "SELECT * FROM applicants ORDER BY applicant_id DESC";
        List<Applicant> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapRowToApplicant(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q2-SELECT ALL] Error: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Q3: Moderate JOIN + WHERE — fetches applicant profile joined with user email.
     */
    public Applicant getApplicantProfile(int applicantId) {
        String sql = "SELECT ap.*, u.email "
                + "FROM applicants ap "
                + "INNER JOIN users u ON ap.user_id = u.id "
                + "WHERE ap.applicant_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, applicantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Applicant applicant = mapRowToApplicant(rs);
                        applicant.setTransientEmail(rs.getString("email"));
                        return applicant;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q3-JOIN-WHERE] Error for applicant_id " + applicantId, e);
        }
        return null;
    }

    public Integer findApplicantIdByUserId(int userId, Connection conn) throws SQLException {
        String sql = "SELECT applicant_id FROM applicants WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("applicant_id");
                }
            }
        }
        return null;
    }

    public Applicant getApplicantByUserId(int userId) {
        String sql = "SELECT ap.*, u.email FROM applicants ap INNER JOIN users u ON ap.user_id = u.id WHERE ap.user_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Applicant applicant = mapRowToApplicant(rs);
                        applicant.setTransientEmail(rs.getString("email"));
                        return applicant;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching applicant by user_id " + userId, e);
        }
        return null;
    }

    public static Applicant mapRowToApplicant(ResultSet rs) throws SQLException {
        String fullName = rs.getString("name");
        return new Applicant(
                rs.getInt("applicant_id"),
                rs.getInt("user_id"),
                NameUtils.splitFirst(fullName),
                NameUtils.splitLast(fullName),
                rs.getString("date_of_birth"),
                rs.getString("place_of_birth"),
                rs.getString("sex"),
                rs.getString("citizenship"),
                rs.getString("contact_no"),
                rs.getString("home_address"),
                rs.getString("civil_status"),
                rs.getString("spouse_name"),
                rs.getString("occupation"),
                rs.getString("employer_office_and_address"),
                rs.getString("father_name"),
                rs.getString("mother_name")
        );
    }
}
