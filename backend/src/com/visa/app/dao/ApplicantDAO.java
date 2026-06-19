package com.visa.app.dao;

import com.visa.app.model.Applicant;
import com.visa.app.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  DAO: ApplicantDAO
 *  QUERIES COVERED:
 *    Q1 (Simple)  — INSERT new applicant into users + applicants
 *    Q2 (Simple)  — SELECT all applicants (SELECT * FROM applicants)
 * ============================================================
 *
 * CORRECTED: previously this DAO inserted/read from a single merged
 * "applications" table that didn't match the ERD. It now writes to
 * `users` (login) and `applicants` (ApplicantT) — exactly the tables
 * that actually exist in the corrected schema.
 */
public class ApplicantDAO {

    private final Connection conn;

    public ApplicantDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 1 — SIMPLE INSERT
    //  Creates the login account (users) and the applicant profile (applicants).
    // ══════════════════════════════════════════════════════════════════════════
    public boolean insertApplicant(Applicant applicant, String email, String password) {
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, 'APPLICANT')";
        String sqlApplicant = "INSERT INTO applicants "
                + "(user_id, name, sex, citizenship, date_of_birth, place_of_birth, contact_no, "
                + " home_address, civil_status, spouse_name, occupation, employer_office_and_address, "
                + " father_name, mother_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, email);
                psUser.setString(2, password);
                psUser.executeUpdate();
                ResultSet keys = psUser.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user ID returned.");
                userId = keys.getInt(1);
                applicant.setUserId(userId);
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
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) applicant.setApplicantId(keys.getInt(1));
            }

            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("[Q1-INSERT] Applicant saved: " + applicant.getProfileSummary());
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.err.println("[Q1-INSERT] Error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 2 — SIMPLE SELECT ALL
    //  Reads every row from `applicants` and maps each to an Applicant model.
    // ══════════════════════════════════════════════════════════════════════════
    public List<Applicant> getAllApplicants() {
        String sql = "SELECT * FROM applicants ORDER BY applicant_id DESC";
        List<Applicant> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToApplicant(rs));
            }
            System.out.println("[Q2-SELECT ALL] Loaded " + list.size() + " applicants.");

        } catch (SQLException e) {
            System.err.println("[Q2-SELECT ALL] Error: " + e.getMessage());
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 3 — MODERATE: JOIN + WHERE (Single-record lookup)
    //  Fetches one applicant's complete personal profile together with their
    //  login email by joining `applicants` and `users` on user_id.
    //  Demonstrates: INNER JOIN, WHERE with a single-value parameter, and
    //  mapping a multi-table row back to a model object.
    //
    //  Used by: admin "View Details" panel, BackendBridge.getApplicantProfile()
    // ══════════════════════════════════════════════════════════════════════════
    public Applicant getApplicantProfile(int applicantId) {
        String sql =
            "SELECT ap.*, u.email " +
            "FROM   applicants ap " +
            "INNER JOIN users u ON ap.user_id = u.id " +
            "WHERE  ap.applicant_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            try (ResultSet rs = ps.executeQuery()) {          // ← ResultSet = our "Cursor"
                if (rs.next()) {                              // single-row result
                    Applicant a = mapRowToApplicant(rs);
                    // Store email as a transient field so the UI can display it
                    a.setTransientEmail(rs.getString("email"));
                    System.out.println("[Q3-JOIN-WHERE] Profile loaded for applicant_id=" + applicantId);
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Q3-JOIN-WHERE] Error: " + e.getMessage());
        }
        return null;
    }

        // ── Private helper: maps one ResultSet row → Applicant OOP model ──────────
    private Applicant mapRowToApplicant(ResultSet rs) throws SQLException {
        String fullName = rs.getString("name");
        return new Applicant(
            rs.getInt   ("applicant_id"),
            rs.getInt   ("user_id"),
            splitFirst(fullName),
            splitLast (fullName),
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

    private String splitFirst(String name) {
        if (name == null || !name.contains(" ")) return name == null ? "" : name;
        return name.substring(0, name.indexOf(' '));
    }

    private String splitLast(String name) {
        if (name == null || !name.contains(" ")) return "";
        return name.substring(name.indexOf(' ') + 1);
    }
}