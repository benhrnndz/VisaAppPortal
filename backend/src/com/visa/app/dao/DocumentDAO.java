package com.visa.app.dao;

import com.visa.app.model.Document;
import com.visa.app.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  DAO: DocumentDAO
 *  QUERIES COVERED:
 *    Q7  (Simple)   — INSERT a new document for an application
 *    Q8  (Moderate) — SELECT documents for one application
 *    Q9  (Difficult) — 3-table JOIN: applicant + application + passport details
 *    Q10 (Difficult) — Subquery: applications that have ALL four document types
 * ============================================================
 */
public class DocumentDAO {

    private final Connection conn;

    public DocumentDAO() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INSERT PASSPORT — into the separate `passports` table
    //  Called by BackendBridge.savePassport()
    // ══════════════════════════════════════════════════════════════════════════
    public boolean insertPassport(String passportNo, String issuedBy,
                                  String dateOfIssue, String validUntil) {
        String sql = "INSERT OR IGNORE INTO passports "
                   + "(passport_no, issued_by, date_of_issue, valid_until) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passportNo);
            ps.setString(2, issuedBy);
            ps.setString(3, dateOfIssue);
            ps.setString(4, validUntil);
            ps.executeUpdate();
            System.out.println("[PASSPORT-INSERT] " + passportNo + " saved.");
            return true;
        } catch (SQLException e) {
            System.err.println("[PASSPORT-INSERT] Error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 7 — SIMPLE INSERT
    //  Adds one supporting document row to the `documents` table.
    // ══════════════════════════════════════════════════════════════════════════
    public boolean insertDocument(Document document) {
        // Per the corrected ERD, documents table only stores (application_id, document_type).
        // Passport rows live in their own `passports` table — NOT here.
        String sql = "INSERT INTO documents (application_id, document_type) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, document.getApplicationId());
            ps.setString(2, document.getDocumentType());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) document.setId(keys.getInt(1));

            System.out.println("[Q7-INSERT] Document saved: " + document.getDocumentSummary());
            return true;

        } catch (SQLException e) {
            System.err.println("[Q7-INSERT] Error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 8 — MODERATE: SELECT with WHERE
    //  Returns all document records for a given application,
    //  mapped into OOP model objects (Passport or SupportingDocument).
    // ══════════════════════════════════════════════════════════════════════════
    public List<Document> getDocumentsByApplicationId(int applicationId) {
        String sql = "SELECT * FROM documents WHERE application_id = ?";
        List<Document> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Document(
                    rs.getInt   ("document_id"),
                    rs.getInt   ("application_id"),
                    rs.getString("document_type")
                ));
            }
            System.out.println("[Q8-SELECT] " + list.size() + " documents for app " + applicationId);

        } catch (SQLException e) {
            System.err.println("[Q8-SELECT] Error: " + e.getMessage());
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 9 — DIFFICULT: 3-TABLE JOIN
    //  Joins users, applications, and documents to retrieve a complete
    //  applicant profile WITH their passport details in one query.
    //  Returns raw string arrays for easy display in a JTable.
    // ══════════════════════════════════════════════════════════════════════════
    public List<String[]> getApplicantsWithPassportDetails() {
        // Q9: 3-table JOIN — users ⟶ applicants ⟶ applications ⟶ passports
        String sql =
            "SELECT u.email, "
          + "       ap.name, ap.citizenship, a.status, "
          + "       p.passport_no, p.issued_by, p.valid_until "
          + "FROM users u "
          + "INNER JOIN applicants   ap ON u.id            = ap.user_id "
          + "INNER JOIN applications a  ON ap.applicant_id = a.applicant_id "
          + "INNER JOIN passports    p  ON a.passport_no   = p.passport_no "
          + "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

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
            System.out.println("[Q9-3-TABLE JOIN] Loaded " + rows.size() + " passport records.");

        } catch (SQLException e) {
            System.err.println("[Q9-3-TABLE JOIN] Error: " + e.getMessage());
        }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 10 — DIFFICULT: SUBQUERY + HAVING
    //  Finds applications that have submitted ALL four required document types:
    //  Original Passport, Air Ticket, Invitation Letter, Bank Certificate.
    //  Uses a subquery with COUNT(DISTINCT) and HAVING to filter complete sets.
    // ══════════════════════════════════════════════════════════════════════════
    public List<String[]> getCompleteApplications() {
        // Q10: applications that have submitted all 3 required supporting documents.
        // (Original Passport is tracked separately in the passports table,
        //  so only the 3 supporting types live in `documents`.)
        String sql =
            "SELECT a.application_id, ap.name, ap.citizenship, a.status "
          + "FROM applications a "
          + "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id "
          + "WHERE a.application_id IN ( "
          + "    SELECT d.application_id "
          + "    FROM documents d "
          + "    WHERE d.document_type IN ('Air Ticket','Invitation Letter','Bank Certificate') "
          + "    GROUP BY d.application_id "
          + "    HAVING COUNT(DISTINCT d.document_type) = 3 "
          + ") "
          + "ORDER BY a.application_id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("application_id")),
                    rs.getString ("name"),
                    rs.getString ("citizenship"),
                    rs.getString ("status")
                });
            }
            System.out.println("[Q10-SUBQUERY] " + rows.size() + " fully-documented applications.");

        } catch (SQLException e) {
            System.err.println("[Q10-SUBQUERY] Error: " + e.getMessage());
        }
        return rows;
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  QUERY 11 — DIFFICULT: CORRELATED SUBQUERY + DATE COMPARISON
    //  Finds applications where the passport expires within 180 days of the
    //  date of application — a real-world validity check visa offices perform.
    //
    //  Demonstrates:
    //    • Correlated subquery (inner SELECT references outer row's passport_no)
    //    • SQLite date arithmetic via julianday()
    //    • Multi-table INNER JOIN (applicants + applications + passports)
    //    • CASE expression for a human-readable urgency label
    //    • ORDER BY computed column alias
    //
    //  Returns rows: [applicant_name, citizenship, passport_no,
    //                 date_of_application, valid_until, days_until_expiry, urgency]
    // ══════════════════════════════════════════════════════════════════════════
    public List<String[]> getApplicationsWithExpiringPassports() {
        String sql =
            "SELECT ap.name, ap.citizenship, " +
            "       a.passport_no, a.date_of_application, " +
            "       p.valid_until, " +
            "       CAST(julianday(p.valid_until) - julianday(a.date_of_application) AS INTEGER) " +
            "           AS days_until_expiry, " +
            "       CASE " +
            "           WHEN julianday(p.valid_until) - julianday(a.date_of_application) < 90  " +
            "               THEN 'CRITICAL' " +
            "           WHEN julianday(p.valid_until) - julianday(a.date_of_application) < 180 " +
            "               THEN 'WARNING' " +
            "           ELSE 'OK' " +
            "       END AS urgency " +
            "FROM applications a " +
            "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
            "INNER JOIN passports  p  ON a.passport_no  = p.passport_no " +
            "WHERE a.passport_no IN ( " +
            "    SELECT p2.passport_no " +           // ← correlated subquery
            "    FROM   passports p2 " +
            "    WHERE  julianday(p2.valid_until) - julianday(a.date_of_application) < 180 " +
            ") " +
            "ORDER BY days_until_expiry ASC";        // most urgent first

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

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
            System.out.println("[Q11-CORRELATED-SUBQUERY] "
                + rows.size() + " applications with near-expiry passports.");

        } catch (SQLException e) {
            System.err.println("[Q11-CORRELATED-SUBQUERY] Error: " + e.getMessage());
        }
        return rows;
    }

}