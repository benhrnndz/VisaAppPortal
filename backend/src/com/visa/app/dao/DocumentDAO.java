package com.visa.app.dao;

import com.visa.app.model.Document;
import com.visa.app.model.Passport;
import com.visa.app.model.SupportingDocument;
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
    //  QUERY 7 — SIMPLE INSERT
    //  Adds one travel document row to the `documents` table.
    // ══════════════════════════════════════════════════════════════════════════
    public boolean insertDocument(Document document) {
        String sql = "INSERT INTO documents "
                   + "(application_id, document_type, passport_number, "
                   + " issuing_authority, date_issued, validity_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, document.getApplicationId());
            ps.setString(2, document.getDocumentType());

            // Passport fields — only populated if the document is a Passport
            if (document instanceof Passport) {
                Passport p = (Passport) document;
                ps.setString(3, p.getPassportNumber());
                ps.setString(4, p.getIssuingAuthority());
                ps.setString(5, p.getDateIssued());
                ps.setString(6, p.getValidityDate());
            } else {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            }

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
            ResultSet rs = ps.executeQuery();       // ← ResultSet = our "Cursor"

            while (rs.next()) {                     // iterate row-by-row
                list.add(mapRowToDocument(rs));
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
        String sql =
            "SELECT u.email, "
          + "       a.full_name, a.citizenship, a.status, "
          + "       d.passport_number, d.issuing_authority, d.validity_date "
          + "FROM users u "
          + "INNER JOIN applications a  ON u.id   = a.user_id "
          + "INNER JOIN documents    d  ON a.id   = d.application_id "
          + "WHERE d.document_type = 'Original Passport' "
          + "ORDER BY a.id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rows.add(new String[]{
                    rs.getString("email"),
                    rs.getString("full_name"),
                    rs.getString("citizenship"),
                    rs.getString("status"),
                    rs.getString("passport_number"),
                    rs.getString("issuing_authority"),
                    rs.getString("validity_date")
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
        String sql =
            "SELECT a.id, a.full_name, a.citizenship, a.status "
          + "FROM applications a "
          + "WHERE a.id IN ( "
          + "    SELECT d.application_id "
          + "    FROM documents d "
          + "    WHERE d.document_type IN "
          + "          ('Original Passport','Air Ticket','Invitation Letter','Bank Certificate') "
          + "    GROUP BY d.application_id "
          + "    HAVING COUNT(DISTINCT d.document_type) = 4 "
          + ") "
          + "ORDER BY a.id DESC";

        List<String[]> rows = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString ("full_name"),
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

    // ── Private helper: maps one row → correct Document subtype ──────────────
    private Document mapRowToDocument(ResultSet rs) throws SQLException {
        int    id            = rs.getInt   ("id");
        int    appId         = rs.getInt   ("application_id");
        String docType       = rs.getString("document_type");
        String passportNum   = rs.getString("passport_number");
        String issuingAuth   = rs.getString("issuing_authority");
        String dateIssued    = rs.getString("date_issued");
        String validityDate  = rs.getString("validity_date");

        // Polymorphic factory: return Passport if type matches, else SupportingDocument
        if ("Original Passport".equalsIgnoreCase(docType)) {
            return new Passport(id, appId, passportNum, issuingAuth, dateIssued, validityDate);
        } else {
            return new SupportingDocument(id, appId, docType);
        }
    }
}
