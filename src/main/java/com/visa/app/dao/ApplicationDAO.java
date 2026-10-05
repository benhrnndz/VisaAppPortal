package com.visa.app.dao;

import com.visa.app.model.Applicant;
import com.visa.app.model.Application;
import com.visa.app.model.ApplicationStatus;
import com.visa.app.model.ApplicationSummaryDTO;

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
 *  DAO: ApplicationDAO
 *  QUERIES COVERED:
 *    Q4 (Simple)   — UPDATE application status (Approve / Deny)
 *    Q5 (Moderate) — SELECT with WHERE + LIKE for search/filter
 *    Q6 (Moderate) — JOIN applications + documents with COUNT aggregate
 * ============================================================
 */
public class ApplicationDAO {

    private static final Logger LOGGER = Logger.getLogger(ApplicationDAO.class.getName());

    /**
     * Q4: Simple Update — Updates status of an application to APPROVED or DENIED.
     */
    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.toUpperCase());
                ps.setInt   (2, applicationId);
                int rows = ps.executeUpdate();
                LOGGER.log(Level.INFO, "[Q4-UPDATE] Status -> {0} for app ID {1}", new Object[]{newStatus, applicationId});
                return rows > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q4-UPDATE] Error: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Q5: Moderate WHERE + LIKE Search — returns full Applicant models.
     */
    public List<Applicant> searchApplications(String keyword) {
        String sql = "SELECT * FROM applicants WHERE name LIKE ? OR citizenship LIKE ? ORDER BY applicant_id DESC";
        List<Applicant> results = new ArrayList<>();
        String wildcard = "%" + keyword + "%";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, wildcard);
                ps.setString(2, wildcard);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(ApplicantDAO.mapRowToApplicant(rs));
                    }
                }
                LOGGER.log(Level.INFO, "[Q5-SEARCH] ''{0}'' -> {1} results.", new Object[]{keyword, results.size()});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q5-SEARCH] Error: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Q5 variant for JTable: returns DTOs with [application_id, name, citizenship, status, doc_count].
     */
    public List<ApplicationSummaryDTO> searchApplicationsAsDTOs(String keyword) {
        String sql =
                "SELECT a.application_id, ap.name, ap.citizenship, a.status, " +
                "       COUNT(d.document_id) AS doc_count " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
                "LEFT  JOIN documents  d  ON a.application_id = d.application_id " +
                "WHERE ap.name        LIKE ? " +
                "   OR ap.citizenship LIKE ? " +
                "   OR a.status       LIKE ? " +
                "GROUP BY a.application_id " +
                "ORDER BY a.application_id DESC";

        List<ApplicationSummaryDTO> dtos = new ArrayList<>();
        String wildcard = "%" + keyword + "%";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, wildcard);
                ps.setString(2, wildcard);
                ps.setString(3, wildcard);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dtos.add(new ApplicationSummaryDTO(
                                rs.getInt("application_id"),
                                rs.getString("name"),
                                rs.getString("citizenship"),
                                rs.getString("status"),
                                rs.getInt("doc_count")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q5-SEARCH-DTOS] Error: " + e.getMessage(), e);
        }
        return dtos;
    }

    public List<String[]> searchApplicationsAsRows(String keyword) {
        List<ApplicationSummaryDTO> dtos = searchApplicationsAsDTOs(keyword);
        List<String[]> rows = new ArrayList<>(dtos.size());
        for (ApplicationSummaryDTO dto : dtos) {
            rows.add(dto.toStringArray());
        }
        return rows;
    }

    /**
     * Q6: Moderate JOIN + GROUP BY + COUNT aggregate.
     */
    public List<String[]> getApplicationsWithDocumentCount() {
        String sql =
                "SELECT a.application_id, ap.name, ap.citizenship, a.status, " +
                "       COUNT(d.document_id) AS doc_count " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id  = ap.applicant_id " +
                "LEFT  JOIN documents  d  ON a.application_id = d.application_id " +
                "GROUP BY a.application_id " +
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
                            rs.getString("status"),
                            String.valueOf(rs.getInt("doc_count"))
                    });
                }
                LOGGER.log(Level.INFO, "[Q6-JOIN+COUNT] Loaded {0} rows.", rows.size());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "[Q6-JOIN+COUNT] Error: " + e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Inserts application record within an existing transaction.
     */
    public int insertApplication(Application app, Connection conn) throws SQLException {
        String sql = "INSERT INTO applications " +
                "(applicant_id, passport_no, requested_entry_type, length_of_stay_days, " +
                " port_of_entry, dest_after_ph, age_upon_application, date_of_application, " +
                " purpose_type, sponsor_name, spon_contact_no, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1,  app.getApplicantId());
            ps.setString(2,  app.getPassportNo());
            ps.setString(3,  app.getRequestedEntryType().isBlank() ? "Single" : app.getRequestedEntryType());
            ps.setInt   (4,  app.getLengthOfStayDays() <= 0 ? 30 : app.getLengthOfStayDays());
            ps.setString(5,  app.getPortOfEntry().isBlank() ? "NAIA" : app.getPortOfEntry());
            ps.setString(6,  app.getDestAfterPH());
            ps.setInt   (7,  app.getAgeUponApplication());
            ps.setString(8,  app.getDateOfApplication().isBlank()
                    ? java.time.LocalDate.now().toString() : app.getDateOfApplication());
            ps.setString(9,  app.getPurposeType().isBlank() ? "Tourism" : app.getPurposeType());
            ps.setString(10, app.getSponsorName());
            ps.setString(11, app.getSponContactNo());
            ps.setString(12, app.getStatus() != null ? app.getStatus().getValue() : "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    app.setApplicationId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to generate application_id");
    }

    public boolean updateApplication(Application app, Connection conn) throws SQLException {
        String sql = "UPDATE applications SET requested_entry_type=?, length_of_stay_days=?, port_of_entry=?, " +
                "dest_after_ph=?, age_upon_application=?, date_of_application=?, " +
                "purpose_type=?, sponsor_name=?, spon_contact_no=?, status=? " +
                "WHERE application_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  app.getRequestedEntryType());
            ps.setInt   (2,  app.getLengthOfStayDays());
            ps.setString(3,  app.getPortOfEntry());
            ps.setString(4,  app.getDestAfterPH());
            ps.setInt   (5,  app.getAgeUponApplication());
            ps.setString(6,  app.getDateOfApplication());
            ps.setString(7,  app.getPurposeType());
            ps.setString(8,  app.getSponsorName());
            ps.setString(9,  app.getSponContactNo());
            ps.setString(10, app.getStatus() != null ? app.getStatus().getValue() : "PENDING");
            ps.setInt   (11, app.getApplicationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteApplication(int applicationId) {
        String sql = "DELETE FROM applications WHERE application_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, applicationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting application " + applicationId, e);
            return false;
        }
    }

    public List<Application> getApplicationsByUserId(int userId) {
        String sql = "SELECT a.*, ap.*, u.email " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
                "INNER JOIN users      u  ON ap.user_id     = u.id " +
                "WHERE ap.user_id = ? " +
                "ORDER BY a.application_id DESC";

        List<Application> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRowToApplication(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading applications for user_id " + userId, e);
        }
        return list;
    }

    public List<Application> getAllApplications() {
        String sql = "SELECT a.*, ap.*, u.email " +
                "FROM applications a " +
                "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
                "INNER JOIN users      u  ON ap.user_id     = u.id " +
                "ORDER BY a.application_id DESC";

        List<Application> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading all applications", e);
        }
        return list;
    }

    public static Application mapRowToApplication(ResultSet rs) throws SQLException {
        Application app = new Application(
                rs.getInt("application_id"),
                rs.getInt("applicant_id"),
                rs.getString("passport_no"),
                rs.getString("requested_entry_type"),
                rs.getInt("length_of_stay_days"),
                rs.getString("port_of_entry"),
                rs.getString("dest_after_ph"),
                rs.getInt("age_upon_application"),
                rs.getString("date_of_application"),
                rs.getString("purpose_type"),
                rs.getString("sponsor_name"),
                rs.getString("spon_contact_no"),
                ApplicationStatus.fromString(rs.getString("status"))
        );

        // Map attached Applicant profile
        Applicant applicant = ApplicantDAO.mapRowToApplicant(rs);
        try {
            applicant.setTransientEmail(rs.getString("email"));
        } catch (SQLException ignored) {}
        app.setApplicant(applicant);

        return app;
    }
}
