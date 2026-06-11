package com.visa.app.dao;

import com.visa.app.model.Applicant;
import com.visa.app.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  DAO: ApplicationDAO
 *  QUERIES COVERED:
 *    Q4 (Simple)   — UPDATE application status (Approve / Deny)
 *    Q5 (Moderate) — SELECT with WHERE + LIKE for search/filter
 *    Q6 (Moderate) — JOIN applications + documents with COUNT aggregate
 * ============================================================
 */
public class ApplicationDAO {

    private final Connection conn;

    public ApplicationDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 4 — SIMPLE UPDATE
    //  Changes the status of an application to APPROVED or DENIED.
    //  Called by the admin panel's Approve/Deny buttons.
    // ══════════════════════════════════════════════════════════════════════════
    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase());
            ps.setInt   (2, applicationId);
            int rows = ps.executeUpdate();
            System.out.println("[Q4-UPDATE] Status → " + newStatus + " for app ID " + applicationId);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[Q4-UPDATE] Error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 5 — MODERATE: WHERE + LIKE (Search/Filter)
    //  Searches applications by name, citizenship, or status keyword.
    //  Demonstrates parameterised wildcard queries.
    // ══════════════════════════════════════════════════════════════════════════
    public List<Applicant> searchApplications(String keyword) {
        String sql = "SELECT * FROM applications "
                   + "WHERE full_name    LIKE ? "
                   + "   OR citizenship  LIKE ? "
                   + "   OR status       LIKE ? "
                   + "ORDER BY id DESC";

        List<Applicant> results = new ArrayList<>();
        String wildcard = "%" + keyword + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);

            ResultSet rs = ps.executeQuery();         // ← ResultSet = our "Cursor"
            while (rs.next()) {                       // row-by-row traversal
                results.add(mapRowToApplicant(rs));
            }
            System.out.println("[Q5-SEARCH] '" + keyword + "' → " + results.size() + " results.");

        } catch (SQLException e) {
            System.err.println("[Q5-SEARCH] Error: " + e.getMessage());
        }
        return results;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 6 — MODERATE: JOIN + GROUP BY + COUNT
    //  Returns each applicant along with how many documents they submitted.
    //  Uses a LEFT JOIN so applicants with 0 documents still appear.
    // ══════════════════════════════════════════════════════════════════════════
    public List<String[]> getApplicationsWithDocumentCount() {
        String sql = "SELECT a.id, a.full_name, a.citizenship, a.status, "
                   + "       COUNT(d.id) AS doc_count "
                   + "FROM applications a "
                   + "LEFT JOIN documents d ON a.id = d.application_id "
                   + "GROUP BY a.id "
                   + "ORDER BY a.id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt   ("id")),
                    rs.getString  ("full_name"),
                    rs.getString  ("citizenship"),
                    rs.getString  ("status"),
                    String.valueOf(rs.getInt   ("doc_count"))
                });
            }
            System.out.println("[Q6-JOIN+COUNT] Loaded " + rows.size() + " rows.");

        } catch (SQLException e) {
            System.err.println("[Q6-JOIN+COUNT] Error: " + e.getMessage());
        }
        return rows;
    }

    // ── Private helper: maps one ResultSet row → Applicant OOP model ──────────
    private Applicant mapRowToApplicant(ResultSet rs) throws SQLException {
        String fullName = rs.getString("full_name");
        return new Applicant(
            rs.getInt   ("user_id"),
            rs.getInt   ("id"),
            splitFirst(fullName),
            splitLast (fullName),
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
