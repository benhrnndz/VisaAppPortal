package com.visa.app;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:visa_app.db";
    private static DatabaseManager instance;

    static {
        try {
            // Force load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        // PRAGMA foreign_keys is a per-connection setting in SQLite — it does NOT
        // persist across connections, so it must be re-enabled here every time,
        // not just once during initializeDatabase(). Without this, ON DELETE
        // CASCADE (e.g. documents -> applications) silently never fires.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Enable foreign key enforcement in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // -------------------------------------------------------------------
            // TABLE 1: users
            // Stores login credentials. Role is either 'APPLICANT' or 'ADMIN'.
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL" +
                    ");");

            // -------------------------------------------------------------------
            // TABLE 2: passports
            // Stores passport details independently (no FK to application).
            // passport_no is the natural primary key.
            // Referenced by applications.passport_no.
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS passports (" +
                    "passport_no TEXT PRIMARY KEY," +
                    "issued_by TEXT NOT NULL," +
                    "date_of_issue TEXT NOT NULL," +
                    "valid_until TEXT NOT NULL" +
                    ");");

            // -------------------------------------------------------------------
            // TABLE 3: applicants
            // Stores personal information about the visa applicant.
            // FK: user_id → users(id)
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS applicants (" +
                    "applicant_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "sex TEXT NOT NULL," +
                    "citizenship TEXT NOT NULL," +
                    "date_of_birth TEXT NOT NULL," +
                    "place_of_birth TEXT NOT NULL," +
                    "contact_no TEXT NOT NULL," +
                    "home_address TEXT NOT NULL," +
                    "civil_status TEXT NOT NULL," +
                    "spouse_name TEXT," +
                    "occupation TEXT," +
                    "employer_office_and_address TEXT," +
                    "father_name TEXT," +
                    "mother_name TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // -------------------------------------------------------------------
            // TABLE 4: applications
            // Stores visa application/travel details.
            // FK: applicant_id → applicants(applicant_id)
            // FK: passport_no  → passports(passport_no)
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                    "application_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "applicant_id INTEGER NOT NULL," +
                    "passport_no TEXT NOT NULL," +
                    "requested_entry_type TEXT NOT NULL," +
                    "length_of_stay_days INTEGER NOT NULL," +
                    "port_of_entry TEXT NOT NULL," +
                    "dest_after_ph TEXT," +
                    "age_upon_application INTEGER NOT NULL," +
                    "date_of_application TEXT NOT NULL," +
                    "purpose_type TEXT NOT NULL," +
                    "sponsor_name TEXT," +
                    "spon_contact_no TEXT," +
                    "status TEXT NOT NULL DEFAULT 'PENDING'," +
                    "FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(passport_no)  REFERENCES passports(passport_no)" +
                    ");");

            // -------------------------------------------------------------------
            // TABLE 5: children
            // Stores children of an applicant.
            // FK: applicant_id → applicants(applicant_id)
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS children (" +
                    "child_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "applicant_id INTEGER NOT NULL," +
                    "child_name TEXT NOT NULL," +
                    "child_age INTEGER NOT NULL," +
                    "FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE" +
                    ");");

            // -------------------------------------------------------------------
            // TABLE 6: documents
            // Stores supporting travel documents per application.
            // Only holds document_type — passport details are in passports table.
            // FK: application_id → applications(application_id)
            // -------------------------------------------------------------------
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (" +
                    "document_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "application_id INTEGER NOT NULL," +
                    "document_type TEXT NOT NULL," +
                    "FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE" +
                    ");");

            // Seed Default Users
            seedDefaultUsers(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedDefaultUsers(Connection conn) {
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE email = 'admin@visa.com'";
        String insertAdminSql = "INSERT INTO users (email, password, role) VALUES ('admin@visa.com', 'admin123', 'ADMIN')";
        String checkApplicantSql = "SELECT COUNT(*) FROM users WHERE email = 'user@visa.com'";
        String insertApplicantSql = "INSERT INTO users (email, password, role) VALUES ('user@visa.com', 'user123', 'APPLICANT')";
        
        try (Statement stmt = conn.createStatement()) {
            // Seed Admin
            try (ResultSet rs = stmt.executeQuery(checkAdminSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(insertAdminSql);
                    System.out.println("Default Admin seeded successfully!");
                }
            }
            // Seed Applicant
            try (ResultSet rs = stmt.executeQuery(checkApplicantSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(insertApplicantSql);
                    System.out.println("Default Applicant seeded successfully!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default users: " + e.getMessage());
        }
    }

    // --- User Operations ---

    public boolean registerUser(String email, String password, String role) {
        String sql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration error (likely email already exists): " + e.getMessage());
            return false;
        }
    }

    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // --- Visa Application Operations ---

    /**
     * Saves a new VisaApplication to the database using the corrected 6-table schema:
     *
     *  Flow:
     *   1. Look up (or create) the applicant row for this user in `applicants`
     *   2. Insert the passport into `passports` (if document type = Original Passport)
     *   3. Insert the trip details into `applications`
     *   4. Insert children into `children` (FK -> applicant_id)
     *   5. Insert supporting documents into `documents` (FK -> application_id)
     */
    public boolean saveApplication(VisaApplication app) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // ── STEP 1: Resolve or create applicant_id ────────────────────────
            // Check if an applicant row already exists for this user
            int applicantId = -1;
            String lookupApplicant = "SELECT applicant_id FROM applicants WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(lookupApplicant)) {
                ps.setInt(1, app.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        applicantId = rs.getInt("applicant_id");
                    }
                }
            }

            if (applicantId == -1) {
                // First application for this user — create the applicant row
                String insertApplicant =
                    "INSERT INTO applicants " +
                    "(user_id, name, sex, citizenship, date_of_birth, place_of_birth, " +
                    " contact_no, home_address, civil_status, spouse_name, occupation, " +
                    " employer_office_and_address, father_name, mother_name) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertApplicant,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt   (1,  app.getUserId());
                    ps.setString(2,  app.getFullName());
                    ps.setString(3,  app.getSex());
                    ps.setString(4,  app.getCitizenship());
                    ps.setString(5,  app.getBirthDate());
                    ps.setString(6,  app.getPlaceOfBirth());
                    ps.setString(7,  app.getContactNumber());
                    ps.setString(8,  app.getHomeAddress());
                    ps.setString(9,  app.getCivilStatus());
                    ps.setString(10, app.getSpouseName());
                    ps.setString(11, app.getOccupation());
                    ps.setString(12, app.getEmployerAddress());
                    ps.setString(13, app.getFatherName());
                    ps.setString(14, app.getMotherName());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) applicantId = keys.getInt(1);
                    }
                }
            }

            // ── STEP 2: Find the passport document and insert into `passports` ─
            String passportNo = "N/A-" + app.getUserId();   // fallback if no passport doc
            if (app.getDocuments() != null) {
                for (Document doc : app.getDocuments()) {
                    if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())
                            && doc.getPassportNumber() != null
                            && !doc.getPassportNumber().isBlank()) {
                        passportNo = doc.getPassportNumber();
                        String upsertPassport =
                            "INSERT OR IGNORE INTO passports " +
                            "(passport_no, issued_by, date_of_issue, valid_until) " +
                            "VALUES (?,?,?,?)";
                        try (PreparedStatement ps = conn.prepareStatement(upsertPassport)) {
                            ps.setString(1, passportNo);
                            ps.setString(2, doc.getIssuingAuthority().isBlank()
                                            ? "Unknown" : doc.getIssuingAuthority());
                            ps.setString(3, doc.getDateIssued().isBlank()
                                            ? "2020/01/01" : doc.getDateIssued());
                            ps.setString(4, doc.getValidityDate().isBlank()
                                            ? "2030/01/01" : doc.getValidityDate());
                            ps.executeUpdate();
                        }
                        break;
                    }
                }
            }
            // Ensure a placeholder passport row exists (NOT NULL FK constraint)
            String ensurePassport =
                "INSERT OR IGNORE INTO passports (passport_no, issued_by, date_of_issue, valid_until) " +
                "VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(ensurePassport)) {
                ps.setString(1, passportNo);
                ps.setString(2, "Pending");
                ps.setString(3, "2020/01/01");
                ps.setString(4, "2030/01/01");
                ps.executeUpdate();
            }

            // ── STEP 3: Insert into `applications` ───────────────────────────
            String insertApp =
                "INSERT INTO applications " +
                "(applicant_id, passport_no, requested_entry_type, length_of_stay_days, " +
                " port_of_entry, dest_after_ph, age_upon_application, date_of_application, " +
                " purpose_type, sponsor_name, spon_contact_no, status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            int applicationId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertApp,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt   (1,  applicantId);
                ps.setString(2,  passportNo);
                ps.setString(3,  app.getEntryType().isBlank() ? "Single" : app.getEntryType());
                ps.setInt   (4,  app.getLengthOfStay() == 0 ? 30 : app.getLengthOfStay());
                ps.setString(5,  app.getPortOfEntry().isBlank() ? "NAIA" : app.getPortOfEntry());
                ps.setString(6,  app.getDestinationAfter());
                ps.setInt   (7,  app.getAgeUponApp());
                ps.setString(8,  app.getDateOfApp().isBlank()
                                  ? java.time.LocalDate.now().toString() : app.getDateOfApp());
                ps.setString(9,  app.getPurposeType().isBlank() ? "Tourism" : app.getPurposeType());
                ps.setString(10, app.getSponsorName());
                ps.setString(11, app.getSponsorContact());
                ps.setString(12, app.getStatus().isBlank() ? "PENDING" : app.getStatus().toUpperCase());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        applicationId = keys.getInt(1);
                        app.setId(applicationId);
                    } else {
                        throw new SQLException("No application_id generated.");
                    }
                }
            }

            // ── STEP 4: Insert children (FK -> applicant_id, not application_id) ─
            if (app.isWithChildren() && app.getChildren() != null) {
                String insertChild =
                    "INSERT INTO children (applicant_id, child_name, child_age) VALUES (?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertChild)) {
                    for (Child child : app.getChildren()) {
                        ps.setInt   (1, applicantId);
                        ps.setString(2, child.getName());
                        ps.setInt   (3, child.getAge());
                        ps.executeUpdate();
                    }
                }
            }

            // ── STEP 5: Insert supporting documents (only document_type, no passport cols) ─
            if (app.getDocuments() != null) {
                String insertDoc =
                    "INSERT INTO documents (application_id, document_type) VALUES (?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertDoc)) {
                    for (Document doc : app.getDocuments()) {
                        // Passport is now stored in `passports`, skip it here
                        if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())) continue;
                        ps.setInt   (1, applicationId);
                        ps.setString(2, doc.getDocumentType());
                        ps.executeUpdate();
                    }
                }
            }

            conn.commit();
            System.out.println("[DB] Application saved. applicant_id=" + applicantId
                + " application_id=" + applicationId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving application: [SQLITE_ERROR] " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean updateApplication(VisaApplication app) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // ── Resolve applicant_id for this user ────────────────────────────
            int applicantId = -1;
            String lookupApplicant = "SELECT applicant_id FROM applicants WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(lookupApplicant)) {
                ps.setInt(1, app.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) applicantId = rs.getInt("applicant_id");
                }
            }
            if (applicantId == -1) {
                throw new SQLException("No applicant record found for user_id=" + app.getUserId());
            }

            // ── Update applicants table (personal info) ───────────────────────
            String updateApplicant =
                "UPDATE applicants SET name=?, sex=?, citizenship=?, date_of_birth=?, " +
                "place_of_birth=?, contact_no=?, home_address=?, civil_status=?, " +
                "spouse_name=?, occupation=?, employer_office_and_address=?, " +
                "father_name=?, mother_name=? WHERE applicant_id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateApplicant)) {
                ps.setString(1,  app.getFullName());
                ps.setString(2,  app.getSex());
                ps.setString(3,  app.getCitizenship());
                ps.setString(4,  app.getBirthDate());
                ps.setString(5,  app.getPlaceOfBirth());
                ps.setString(6,  app.getContactNumber());
                ps.setString(7,  app.getHomeAddress());
                ps.setString(8,  app.getCivilStatus());
                ps.setString(9,  app.getSpouseName());
                ps.setString(10, app.getOccupation());
                ps.setString(11, app.getEmployerAddress());
                ps.setString(12, app.getFatherName());
                ps.setString(13, app.getMotherName());
                ps.setInt   (14, applicantId);
                ps.executeUpdate();
            }

            // ── Update applications table (trip details) ──────────────────────
            String updateApp =
                "UPDATE applications SET " +
                "requested_entry_type=?, length_of_stay_days=?, port_of_entry=?, " +
                "dest_after_ph=?, age_upon_application=?, date_of_application=?, " +
                "purpose_type=?, sponsor_name=?, spon_contact_no=?, status=? " +
                "WHERE application_id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateApp)) {
                ps.setString(1,  app.getEntryType());
                ps.setInt   (2,  app.getLengthOfStay());
                ps.setString(3,  app.getPortOfEntry());
                ps.setString(4,  app.getDestinationAfter());
                ps.setInt   (5,  app.getAgeUponApp());
                ps.setString(6,  app.getDateOfApp());
                ps.setString(7,  app.getPurposeType());
                ps.setString(8,  app.getSponsorName());
                ps.setString(9,  app.getSponsorContact());
                ps.setString(10, app.getStatus());
                ps.setInt   (11, app.getId());
                ps.executeUpdate();
            }

            // ── Replace children ──────────────────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM children WHERE applicant_id = ?")) {
                ps.setInt(1, applicantId);
                ps.executeUpdate();
            }
            if (app.isWithChildren() && app.getChildren() != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO children (applicant_id, child_name, child_age) VALUES (?,?,?)")) {
                    for (Child child : app.getChildren()) {
                        ps.setInt   (1, applicantId);
                        ps.setString(2, child.getName());
                        ps.setInt   (3, child.getAge());
                        ps.executeUpdate();
                    }
                }
            }

            // ── Replace supporting documents ──────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM documents WHERE application_id = ?")) {
                ps.setInt(1, app.getId());
                ps.executeUpdate();
            }
            if (app.getDocuments() != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO documents (application_id, document_type) VALUES (?,?)")) {
                    for (Document doc : app.getDocuments()) {
                        if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())) continue;
                        ps.setInt   (1, app.getId());
                        ps.setString(2, doc.getDocumentType());
                        ps.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating application: " + e.getMessage());
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean deleteApplication(int appId) {
        String sql = "DELETE FROM applications WHERE application_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting application: " + e.getMessage());
            return false;
        }
    }

    public boolean updateApplicationStatus(int appId, String status) {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.toUpperCase());
            pstmt.setInt(2, appId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating application status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all applications submitted by a given user_id.
     * Joins applicants + applications + passports to rebuild a VisaApplication object.
     */
    public List<VisaApplication> getApplicationsByUserId(int userId) {
        String sql =
            "SELECT a.application_id, a.passport_no, a.requested_entry_type, " +
            "       a.length_of_stay_days, a.port_of_entry, a.dest_after_ph, " +
            "       a.age_upon_application, a.date_of_application, a.purpose_type, " +
            "       a.sponsor_name, a.spon_contact_no, a.status, " +
            "       ap.applicant_id, ap.user_id, ap.name, ap.sex, ap.citizenship, " +
            "       ap.date_of_birth, ap.place_of_birth, ap.contact_no, ap.home_address, " +
            "       ap.civil_status, ap.spouse_name, ap.occupation, " +
            "       ap.employer_office_and_address, ap.father_name, ap.mother_name, " +
            "       u.email " +
            "FROM applications a " +
            "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
            "INNER JOIN users      u  ON ap.user_id     = u.id " +
            "WHERE ap.user_id = ? " +
            "ORDER BY a.application_id DESC";

        List<VisaApplication> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading applications by user: [SQLITE_ERROR] " + e.getMessage());
        }
        return list;
    }

    public List<VisaApplication> getAllApplications() {
        String sql =
            "SELECT a.application_id, a.passport_no, a.requested_entry_type, " +
            "       a.length_of_stay_days, a.port_of_entry, a.dest_after_ph, " +
            "       a.age_upon_application, a.date_of_application, a.purpose_type, " +
            "       a.sponsor_name, a.spon_contact_no, a.status, " +
            "       ap.applicant_id, ap.user_id, ap.name, ap.sex, ap.citizenship, " +
            "       ap.date_of_birth, ap.place_of_birth, ap.contact_no, ap.home_address, " +
            "       ap.civil_status, ap.spouse_name, ap.occupation, " +
            "       ap.employer_office_and_address, ap.father_name, ap.mother_name, " +
            "       u.email " +
            "FROM applications a " +
            "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
            "INNER JOIN users      u  ON ap.user_id     = u.id " +
            "ORDER BY a.application_id DESC";

        List<VisaApplication> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToApplication(conn, rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading all applications: " + e.getMessage());
        }
        return list;
    }

    public List<VisaApplication> searchApplications(String query) {
        String sql =
            "SELECT a.application_id, a.passport_no, a.requested_entry_type, " +
            "       a.length_of_stay_days, a.port_of_entry, a.dest_after_ph, " +
            "       a.age_upon_application, a.date_of_application, a.purpose_type, " +
            "       a.sponsor_name, a.spon_contact_no, a.status, " +
            "       ap.applicant_id, ap.user_id, ap.name, ap.sex, ap.citizenship, " +
            "       ap.date_of_birth, ap.place_of_birth, ap.contact_no, ap.home_address, " +
            "       ap.civil_status, ap.spouse_name, ap.occupation, " +
            "       ap.employer_office_and_address, ap.father_name, ap.mother_name, " +
            "       u.email " +
            "FROM applications a " +
            "INNER JOIN applicants ap ON a.applicant_id = ap.applicant_id " +
            "INNER JOIN users      u  ON ap.user_id     = u.id " +
            "WHERE ap.name LIKE ? OR u.email LIKE ? OR a.status LIKE ? " +
            "ORDER BY a.application_id DESC";

        List<VisaApplication> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String w = "%" + query + "%";
            pstmt.setString(1, w);
            pstmt.setString(2, w);
            pstmt.setString(3, w);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching applications: " + e.getMessage());
        }
        return list;
    }

    /**
     * Maps a ResultSet row (from the JOIN queries above) back to a VisaApplication.
     * Column aliases match the new schema column names exactly.
     */
    private VisaApplication mapResultSetToApplication(Connection conn, ResultSet rs)
            throws SQLException {
        int applicationId = rs.getInt("application_id");
        int applicantId   = rs.getInt("applicant_id");

        VisaApplication app = new VisaApplication(
                applicationId,
                rs.getInt   ("user_id"),
                rs.getString("name"),           // ap.name
                rs.getString("sex"),
                rs.getString("citizenship"),
                rs.getString("civil_status"),
                rs.getString("date_of_birth"),  // ap.date_of_birth
                rs.getString("place_of_birth"),
                rs.getString("email"),          // u.email
                rs.getString("contact_no"),     // ap.contact_no
                rs.getString("home_address"),
                rs.getString("father_name"),
                rs.getString("mother_name"),
                rs.getString("spouse_name"),
                false,                          // with_children computed below
                rs.getString("occupation"),
                rs.getString("employer_office_and_address"),
                rs.getString("status")
        );

        app.setEntryType      (rs.getString("requested_entry_type"));
        app.setLengthOfStay   (rs.getInt   ("length_of_stay_days"));
        app.setPortOfEntry    (rs.getString("port_of_entry"));
        app.setDestinationAfter(rs.getString("dest_after_ph"));
        app.setAgeUponApp     (rs.getInt   ("age_upon_application"));
        app.setDateOfApp      (rs.getString("date_of_application"));
        app.setPurposeType    (rs.getString("purpose_type"));
        app.setSponsorName    (rs.getString("sponsor_name"));
        app.setSponsorContact (rs.getString("spon_contact_no"));

        // ── Load children (FK is applicant_id in new schema) ─────────────────
        String sqlChildren = "SELECT * FROM children WHERE applicant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlChildren)) {
            ps.setInt(1, applicantId);
            try (ResultSet rsChild = ps.executeQuery()) {
                while (rsChild.next()) {
                    app.addChild(new Child(
                            rsChild.getInt   ("child_id"),
                            applicationId,
                            rsChild.getString("child_name"),
                            rsChild.getInt   ("child_age")
                    ));
                    app.setWithChildren(true);
                }
            }
        }

        // ── Load supporting documents ─────────────────────────────────────────
        // Also load the passport from the passports table and represent it as a Document
        // so the existing UI (which expects a List<Document>) still works.
        String passportNo = rs.getString("passport_no");
        if (passportNo != null && !passportNo.startsWith("N/A-")) {
            String sqlPassport = "SELECT * FROM passports WHERE passport_no = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPassport)) {
                ps.setString(1, passportNo);
                try (ResultSet rsPp = ps.executeQuery()) {
                    if (rsPp.next()) {
                        app.addDocument(new Document(
                                -1, applicationId,
                                "Original Passport",
                                passportNo,
                                rsPp.getString("issued_by"),
                                rsPp.getString("date_of_issue"),
                                rsPp.getString("valid_until")
                        ));
                    }
                }
            }
        }

        String sqlDocs = "SELECT * FROM documents WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDocs)) {
            ps.setInt(1, applicationId);
            try (ResultSet rsDoc = ps.executeQuery()) {
                while (rsDoc.next()) {
                    app.addDocument(new Document(
                            rsDoc.getInt   ("document_id"),
                            applicationId,
                            rsDoc.getString("document_type"),
                            null, null, null, null   // no passport fields on supporting docs
                    ));
                }
            }
        }

        return app;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}