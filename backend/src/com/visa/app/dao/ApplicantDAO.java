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
 *    Q1 (Simple)   — INSERT new applicant into users + applications
 *    Q2 (Simple)   — SELECT all applicants (SELECT * FROM applications)
 *    Q3 (Moderate) — JOIN users + applications to get applicant with email
 * ============================================================
 *
 * A DAO (Data Access Object) isolates every SQL query from the UI layer.
 * The Swing panels call these methods and receive Java objects back —
 * they never write a single SQL string themselves.
 */
public class ApplicantDAO {

    private final Connection conn;

    public ApplicantDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 1 — SIMPLE INSERT
    //  Saves a new applicant's user account and application record.
    //  Uses PreparedStatement to prevent SQL injection.
    // ══════════════════════════════════════════════════════════════════════════
    public boolean insertApplicant(Applicant applicant, String password) {
        // Step 1: insert into users table
        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, 'APPLICANT')";
        // Step 2: insert into applications table
        String sqlApp  = "INSERT INTO applications "
                       + "(user_id, full_name, sex, citizenship, civil_status, birth_date, "
                       + "place_of_birth, email, contact_number, home_address, status) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try {
            conn.setAutoCommit(false);

            // Insert user
            int userId;
            try (PreparedStatement psUser = conn.prepareStatement(
                    sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, applicant.getEmail());
                psUser.setString(2, password);
                psUser.executeUpdate();
                ResultSet keys = psUser.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user ID returned.");
                userId = keys.getInt(1);
                applicant.setUserId(userId);
            }

            // Insert application
            try (PreparedStatement psApp = conn.prepareStatement(
                    sqlApp, Statement.RETURN_GENERATED_KEYS)) {
                psApp.setInt   (1,  userId);
                psApp.setString(2,  applicant.getFullName());
                psApp.setString(3,  applicant.getSex());
                psApp.setString(4,  applicant.getCitizenship());
                psApp.setString(5,  applicant.getCivilStatus());
                psApp.setString(6,  applicant.getDateOfBirth());
                psApp.setString(7,  applicant.getPlaceOfBirth());
                psApp.setString(8,  applicant.getEmail());
                psApp.setString(9,  applicant.getContactNumber());
                psApp.setString(10, applicant.getHomeAddress());
                psApp.executeUpdate();
                ResultSet keys = psApp.getGeneratedKeys();
                if (keys.next()) applicant.setApplicationId(keys.getInt(1));
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
    //  Reads every application row and maps each to an Applicant model.
    //  Cursor equivalent: we iterate the ResultSet row-by-row.
    // ══════════════════════════════════════════════════════════════════════════
    public List<Applicant> getAllApplicants() {
        String sql = "SELECT * FROM applications ORDER BY id DESC";
        List<Applicant> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {           // ← ResultSet = our "Cursor"

            while (rs.next()) {                                   // move row-by-row
                list.add(mapRowToApplicant(rs));
            }
            System.out.println("[Q2-SELECT ALL] Loaded " + list.size() + " applicants.");

        } catch (SQLException e) {
            System.err.println("[Q2-SELECT ALL] Error: " + e.getMessage());
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 3 — MODERATE: INNER JOIN
    //  Joins users + applications to retrieve the applicant record
    //  alongside the registered email address.
    // ══════════════════════════════════════════════════════════════════════════
    public Applicant getApplicantWithEmail(int applicationId) {
        String sql = "SELECT a.*, u.email AS user_email "
                   + "FROM applications a "
                   + "INNER JOIN users u ON a.user_id = u.id "
                   + "WHERE a.id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Applicant applicant = mapRowToApplicant(rs);
                // The JOIN gives us the email column from users
                applicant.setEmail(rs.getString("user_email"));
                System.out.println("[Q3-JOIN] " + applicant.getProfileSummary());
                return applicant;
            }
        } catch (SQLException e) {
            System.err.println("[Q3-JOIN] Error: " + e.getMessage());
        }
        return null;
    }

    // ── Private helper: maps one ResultSet row → Applicant OOP model ──────────
    private Applicant mapRowToApplicant(ResultSet rs) throws SQLException {
        return new Applicant(
            rs.getInt   ("user_id"),
            rs.getInt   ("id"),
            // Split full_name into firstName / lastName at the first space
            splitFirst(rs.getString("full_name")),
            splitLast (rs.getString("full_name")),
            rs.getString("birth_date"),
            rs.getString("email"),
            rs.getString("contact_number"),
            rs.getString("sex"),
            rs.getString("citizenship"),
            rs.getString("civil_status"),
            rs.getString("place_of_birth"),
            rs.getString("home_address"),
            rs.getString("status")
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
