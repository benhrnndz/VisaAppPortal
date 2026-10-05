package com.visa.app.dao;

import com.visa.app.model.Document;

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
 *  DAO: DocumentDAO
 *  QUERIES COVERED:
 *    Q7  (Simple)   — INSERT a new document for an application
 *    Q8  (Moderate) — SELECT documents for one application
 *    Q9  (Difficult)— 3-table JOIN: users + applicants + applications + passports
 *    Q10 (Difficult)— Subquery + HAVING: applications with all 3 supporting docs
 *    Q11 (Difficult)— Correlated subquery: passports expiring within 180 days
 * ============================================================
 */
public class DocumentDAO {

    private static final Logger LOGGER = Logger.getLogger(DocumentDAO.class.getName());

    /**
     * Q7: Simple Insert — adds one supporting document row.
     */
    public boolean insertDocument(Document document) {
        String sql = "INSERT INTO documents (application_id, document_type) VALUES (?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt   (1, document.getApplicationId());
                ps.setString(2, document.getDocumentType());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) document.setId(keys.getInt(1));
                }
                LOGGER.log(Level.INFO, "[Q7-INSERT] Document saved: {0}", document.getDocumentSummary());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q7-INSERT] Error saving document: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean insertDocument(Document document, Connection conn) throws SQLException {
        String sql = "INSERT INTO documents (application_id, document_type) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, document.getApplicationId());
            ps.setString(2, document.getDocumentType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) document.setId(keys.getInt(1));
            }
            return true;
        }
    }

    /**
     * Q8: Moderate Select — retrieves supporting documents for an application.
     */
    public List<Document> getDocumentsByApplicationId(int applicationId) {
        String sql = "SELECT document_id, application_id, document_type FROM documents WHERE application_id = ?";
        List<Document> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, applicationId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Document(
                                rs.getInt("document_id"),
                                rs.getInt("application_id"),
                                rs.getString("document_type")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q8-SELECT] Error for app " + applicationId, e);
        }
        return list;
    }

    public void deleteDocumentsByApplicationId(int applicationId, Connection conn) throws SQLException {
        String sql = "DELETE FROM documents WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.executeUpdate();
        }
    }

    /**
     * Q9: Difficult 3-table JOIN — retrieves applicant profile with passport details.
     */
    public List<String[]> getApplicantsWithPassportDetails() {
        String sql =
                "SELECT u.email, ap.name, ap.citizenship, a.status, " +
                "       p.passport_no, p.issued_by, p.valid_until " +
                "FROM users u " +
                "INNER JOIN applicants   ap ON u.id            = ap.user_id " +
                "INNER JOIN applications a  ON ap.applicant_id = a.applicant_id " +
                "INNER JOIN passports    p  ON a.passport_no   = p.passport_no " +
                "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getString("citizenship"),
                            rs.getString("status"),
                            rs.getString("passport_no"),
                            rs.getString("issued_by"),
                            rs.getString("valid_until")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q9-3-TABLE JOIN] Error: " + e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Q10: Difficult Subquery + HAVING — applications with all 3 supporting document types.
     */
    public List<String[]> getCompleteApplications() {
        String sql =
                "SELECT a.application_id, ap.name, ap.citizenship, a.status " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
                "WHERE a.application_id IN ( " +
                "    SELECT d.application_id " +
                "    FROM documents d " +
                "    WHERE d.document_type IN ('Air Ticket', 'Invitation Letter', 'Bank Certificate') " +
                "    GROUP BY d.application_id " +
                "    HAVING COUNT(DISTINCT d.document_type) = 3 " +
                ") " +
                "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    rows.add(new String[]{
                            String.valueOf(rs.getInt("application_id")),
                            rs.getString("name"),
                            rs.getString("citizenship"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q10-SUBQUERY] Error: " + e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Q11: Difficult Correlated Subquery + Date comparison using SQLite julianday().
     */
    public List<String[]> getApplicationsWithExpiringPassports() {
        String sql =
                "SELECT ap.name, ap.citizenship, a.passport_no, a.date_of_application, p.valid_until, " +
                "       CAST(julianday(p.valid_until) - julianday(a.date_of_application) AS INTEGER) AS days_until_expiry, " +
                "       CASE " +
                "           WHEN julianday(p.valid_until) - julianday(a.date_of_application) < 90  THEN 'CRITICAL' " +
                "           WHEN julianday(p.valid_until) - julianday(a.date_of_application) < 180 THEN 'WARNING' " +
                "           ELSE 'OK' " +
                "       END AS urgency " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
                "INNER JOIN passports  p  ON a.passport_no  = p.passport_no " +
                "WHERE a.passport_no IN ( " +
                "    SELECT p2.passport_no FROM passports p2 " +
                "    WHERE julianday(p2.valid_until) - julianday(a.date_of_application) < 180 " +
                ") " +
                "ORDER BY days_until_expiry ASC";

        List<String[]> rows = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    rows.add(new String[]{
                            rs.getString("name"),
                            rs.getString("citizenship"),
                            rs.getString("passport_no"),
                            rs.getString("date_of_application"),
                            rs.getString("valid_until"),
                            String.valueOf(rs.getInt("days_until_expiry")),
                            rs.getString("urgency")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q11-CORRELATED-SUBQUERY] Error: " + e.getMessage(), e);
        }
        return rows;
    }
}
