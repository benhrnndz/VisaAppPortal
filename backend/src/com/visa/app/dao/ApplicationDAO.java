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
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";

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
    //  Searches applicants by name or citizenship keyword.
    // ══════════════════════════════════════════════════════════════════════════
    public List<Applicant> searchApplications(String keyword) {
        String sql = "SELECT * FROM applicants "
                   + "WHERE name        LIKE ? "
                   + "   OR citizenship LIKE ? "
                   + "ORDER BY applicant_id DESC";

        List<Applicant> results = new ArrayList<>();
        String wildcard = "%" + keyword + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapRowToApplicant(rs));
            }
            System.out.println("[Q5-SEARCH] '" + keyword + "' → " + results.size() + " results.");
        } catch (SQLException e) {
            System.err.println("[Q5-SEARCH] Error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Q5 variant for the admin table — returns String[] rows:
     * [application_id, name, citizenship, status, doc_count].
     */
    public List<String[]> searchApplicationsAsRows(String keyword) {
        String sql =
            "SELECT a.application_id, ap.name, ap.citizenship, a.status, "
          + "       COUNT(d.document_id) AS doc_count "
          + "FROM applications a "
          + "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id "
          + "LEFT  JOIN documents  d  ON a.application_id = d.application_id "
          + "WHERE ap.name        LIKE ? "
          + "   OR ap.citizenship LIKE ? "
          + "   OR a.status       LIKE ? "
          + "GROUP BY a.application_id "
          + "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();
        String wildcard = "%" + keyword + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("application_id")),
                    rs.getString("name"),
                    rs.getString("citizenship"),
                    rs.getString("status"),
                    String.valueOf(rs.getInt("doc_count"))
                });
            }
            System.out.println("[Q5-SEARCH-ROWS] '" + keyword + "' → " + rows.size() + " results.");
        } catch (SQLException e) {
            System.err.println("[Q5-SEARCH-ROWS] Error: " + e.getMessage());
        }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 6 — MODERATE: JOIN + GROUP BY + COUNT
    //  Returns each application with applicant name, citizenship, status, and
    //  how many supporting documents they submitted.
    // ══════════════════════════════════════════════════════════════════════════
    public List<String[]> getApplicationsWithDocumentCount() {
        String sql =
            "SELECT a.application_id, ap.name, ap.citizenship, a.status, "
          + "       COUNT(d.document_id) AS doc_count "
          + "FROM applications a "
          + "INNER JOIN applicants ap ON a.applicant_id  = ap.applicant_id "
          + "LEFT  JOIN documents  d  ON a.application_id = d.application_id "
          + "GROUP BY a.application_id "
          + "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt   ("application_id")),
                    rs.getString  ("name"),
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