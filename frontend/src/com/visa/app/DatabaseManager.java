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
        return DriverManager.getConnection(DB_URL);
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

    public boolean saveApplication(VisaApplication app) {
        Connection conn = null;
        PreparedStatement pstmtApp = null;
        PreparedStatement pstmtChild = null;
        PreparedStatement pstmtDoc = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Insert Application
            String sqlApp = "INSERT INTO applications (user_id, full_name, sex, citizenship, civil_status, birth_date, " +
                    "place_of_birth, email, contact_number, home_address, father_name, mother_name, spouse_name, " +
                    "with_children, occupation, employer_address, status, " +
                    "entry_type, length_of_stay, port_of_entry, destination_after, age_upon_app, date_of_app, purpose_type, sponsor_name, sponsor_contact) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmtApp = conn.prepareStatement(sqlApp, Statement.RETURN_GENERATED_KEYS);
            pstmtApp.setInt(1, app.getUserId());
            pstmtApp.setString(2, app.getFullName());
            pstmtApp.setString(3, app.getSex());
            pstmtApp.setString(4, app.getCitizenship());
            pstmtApp.setString(5, app.getCivilStatus());
            pstmtApp.setString(6, app.getBirthDate());
            pstmtApp.setString(7, app.getPlaceOfBirth());
            pstmtApp.setString(8, app.getEmail());
            pstmtApp.setString(9, app.getContactNumber());
            pstmtApp.setString(10, app.getHomeAddress());
            pstmtApp.setString(11, app.getFatherName());
            pstmtApp.setString(12, app.getMotherName());
            pstmtApp.setString(13, app.getSpouseName());
            pstmtApp.setBoolean(14, app.isWithChildren());
            pstmtApp.setString(15, app.getOccupation());
            pstmtApp.setString(16, app.getEmployerAddress());
            pstmtApp.setString(17, app.getStatus());
            pstmtApp.setString(18, app.getEntryType());
            pstmtApp.setInt(19, app.getLengthOfStay());
            pstmtApp.setString(20, app.getPortOfEntry());
            pstmtApp.setString(21, app.getDestinationAfter());
            pstmtApp.setInt(22, app.getAgeUponApp());
            pstmtApp.setString(23, app.getDateOfApp());
            pstmtApp.setString(24, app.getPurposeType());
            pstmtApp.setString(25, app.getSponsorName());
            pstmtApp.setString(26, app.getSponsorContact());
            pstmtApp.executeUpdate();

            generatedKeys = pstmtApp.getGeneratedKeys();
            int applicationId = -1;
            if (generatedKeys.next()) {
                applicationId = generatedKeys.getInt(1);
                app.setId(applicationId);
            } else {
                throw new SQLException("Creating visa application failed, no ID obtained.");
            }

            // 2. Insert Children
            if (app.isWithChildren() && app.getChildren() != null) {
                String sqlChild = "INSERT INTO children (application_id, name, age) VALUES (?, ?, ?)";
                pstmtChild = conn.prepareStatement(sqlChild);
                for (Child child : app.getChildren()) {
                    pstmtChild.setInt(1, applicationId);
                    pstmtChild.setString(2, child.getName());
                    pstmtChild.setInt(3, child.getAge());
                    pstmtChild.executeUpdate();
                }
            }

            // 3. Insert Documents
            if (app.getDocuments() != null) {
                String sqlDoc = "INSERT INTO documents (application_id, document_type, passport_number, issuing_authority, " +
                        "date_issued, validity_date) VALUES (?, ?, ?, ?, ?, ?)";
                pstmtDoc = conn.prepareStatement(sqlDoc);
                for (Document doc : app.getDocuments()) {
                    pstmtDoc.setInt(1, applicationId);
                    pstmtDoc.setString(2, doc.getDocumentType());
                    pstmtDoc.setString(3, doc.getPassportNumber());
                    pstmtDoc.setString(4, doc.getIssuingAuthority());
                    pstmtDoc.setString(5, doc.getDateIssued());
                    pstmtDoc.setString(6, doc.getValidityDate());
                    pstmtDoc.executeUpdate();
                }
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving application: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(pstmtApp);
            closeQuietly(pstmtChild);
            closeQuietly(pstmtDoc);
            closeQuietly(conn);
        }
    }

    public boolean updateApplication(VisaApplication app) {
        Connection conn = null;
        PreparedStatement pstmtApp = null;
        PreparedStatement pstmtDelChildren = null;
        PreparedStatement pstmtDelDocs = null;
        PreparedStatement pstmtChild = null;
        PreparedStatement pstmtDoc = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Update applications table
            String sqlApp = "UPDATE applications SET full_name=?, sex=?, citizenship=?, civil_status=?, birth_date=?, " +
                    "place_of_birth=?, email=?, contact_number=?, home_address=?, father_name=?, mother_name=?, " +
                    "spouse_name=?, with_children=?, occupation=?, employer_address=?, status=?, " +
                    "entry_type=?, length_of_stay=?, port_of_entry=?, destination_after=?, age_upon_app=?, date_of_app=?, purpose_type=?, sponsor_name=?, sponsor_contact=? WHERE id=?";
            pstmtApp = conn.prepareStatement(sqlApp);
            pstmtApp.setString(1, app.getFullName());
            pstmtApp.setString(2, app.getSex());
            pstmtApp.setString(3, app.getCitizenship());
            pstmtApp.setString(4, app.getCivilStatus());
            pstmtApp.setString(5, app.getBirthDate());
            pstmtApp.setString(6, app.getPlaceOfBirth());
            pstmtApp.setString(7, app.getEmail());
            pstmtApp.setString(8, app.getContactNumber());
            pstmtApp.setString(9, app.getHomeAddress());
            pstmtApp.setString(10, app.getFatherName());
            pstmtApp.setString(11, app.getMotherName());
            pstmtApp.setString(12, app.getSpouseName());
            pstmtApp.setBoolean(13, app.isWithChildren());
            pstmtApp.setString(14, app.getOccupation());
            pstmtApp.setString(15, app.getEmployerAddress());
            pstmtApp.setString(16, app.getStatus());
            pstmtApp.setString(17, app.getEntryType());
            pstmtApp.setInt(18, app.getLengthOfStay());
            pstmtApp.setString(19, app.getPortOfEntry());
            pstmtApp.setString(20, app.getDestinationAfter());
            pstmtApp.setInt(21, app.getAgeUponApp());
            pstmtApp.setString(22, app.getDateOfApp());
            pstmtApp.setString(23, app.getPurposeType());
            pstmtApp.setString(24, app.getSponsorName());
            pstmtApp.setString(25, app.getSponsorContact());
            pstmtApp.setInt(26, app.getId());
            pstmtApp.executeUpdate();

            // 2. Delete old children and insert updated list
            String sqlDelChildren = "DELETE FROM children WHERE application_id = ?";
            pstmtDelChildren = conn.prepareStatement(sqlDelChildren);
            pstmtDelChildren.setInt(1, app.getId());
            pstmtDelChildren.executeUpdate();

            if (app.isWithChildren() && app.getChildren() != null) {
                String sqlChild = "INSERT INTO children (application_id, name, age) VALUES (?, ?, ?)";
                pstmtChild = conn.prepareStatement(sqlChild);
                for (Child child : app.getChildren()) {
                    pstmtChild.setInt(1, app.getId());
                    pstmtChild.setString(2, child.getName());
                    pstmtChild.setInt(3, child.getAge());
                    pstmtChild.executeUpdate();
                }
            }

            // 3. Delete old documents and insert updated list
            String sqlDelDocs = "DELETE FROM documents WHERE application_id = ?";
            pstmtDelDocs = conn.prepareStatement(sqlDelDocs);
            pstmtDelDocs.setInt(1, app.getId());
            pstmtDelDocs.executeUpdate();

            if (app.getDocuments() != null) {
                String sqlDoc = "INSERT INTO documents (application_id, document_type, passport_number, issuing_authority, " +
                        "date_issued, validity_date) VALUES (?, ?, ?, ?, ?, ?)";
                pstmtDoc = conn.prepareStatement(sqlDoc);
                for (Document doc : app.getDocuments()) {
                    pstmtDoc.setInt(1, app.getId());
                    pstmtDoc.setString(2, doc.getDocumentType());
                    pstmtDoc.setString(3, doc.getPassportNumber());
                    pstmtDoc.setString(4, doc.getIssuingAuthority());
                    pstmtDoc.setString(5, doc.getDateIssued());
                    pstmtDoc.setString(6, doc.getValidityDate());
                    pstmtDoc.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating application: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            closeQuietly(pstmtApp);
            closeQuietly(pstmtDelChildren);
            closeQuietly(pstmtDelDocs);
            closeQuietly(pstmtChild);
            closeQuietly(pstmtDoc);
            closeQuietly(conn);
        }
    }

    public boolean deleteApplication(int appId) {
        String sql = "DELETE FROM applications WHERE id = ?";
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
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
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

    public List<VisaApplication> getApplicationsByUserId(int userId) {
        String email = "";
        String userSql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(userSql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading user email: " + e.getMessage());
        }

        String sql = "SELECT * FROM applications WHERE user_id = ? AND email = ? ORDER BY id DESC";
        List<VisaApplication> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading applications by user: " + e.getMessage());
        }
        return list;
    }

    public List<VisaApplication> getAllApplications() {
        String sql = "SELECT * FROM applications ORDER BY id DESC";
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
        String sql = "SELECT * FROM applications WHERE full_name LIKE ? OR email LIKE ? OR status LIKE ? ORDER BY id DESC";
        List<VisaApplication> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcard = "%" + query + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);
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

    private VisaApplication mapResultSetToApplication(Connection conn, ResultSet rs) throws SQLException {
        int appId = rs.getInt("id");
        
        VisaApplication app = new VisaApplication(
                appId,
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("sex"),
                rs.getString("citizenship"),
                rs.getString("civil_status"),
                rs.getString("birth_date"),
                rs.getString("place_of_birth"),
                rs.getString("email"),
                rs.getString("contact_number"),
                rs.getString("home_address"),
                rs.getString("father_name"),
                rs.getString("mother_name"),
                rs.getString("spouse_name"),
                rs.getBoolean("with_children") || (rs.getInt("with_children") == 1), // SQLite compatibility
                rs.getString("occupation"),
                rs.getString("employer_address"),
                rs.getString("status")
        );

        app.setEntryType(rs.getString("entry_type"));
        app.setLengthOfStay(rs.getInt("length_of_stay"));
        app.setPortOfEntry(rs.getString("port_of_entry"));
        app.setDestinationAfter(rs.getString("destination_after"));
        app.setAgeUponApp(rs.getInt("age_upon_app"));
        app.setDateOfApp(rs.getString("date_of_app"));
        app.setPurposeType(rs.getString("purpose_type"));
        app.setSponsorName(rs.getString("sponsor_name"));
        app.setSponsorContact(rs.getString("sponsor_contact"));

        // Load children
        String sqlChildren = "SELECT * FROM children WHERE application_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlChildren)) {
            pstmt.setInt(1, appId);
            try (ResultSet rsChild = pstmt.executeQuery()) {
                while (rsChild.next()) {
                    app.addChild(new Child(
                            rsChild.getInt("id"),
                            appId,
                            rsChild.getString("name"),
                            rsChild.getInt("age")
                    ));
                }
            }
        }

        // Load documents
        String sqlDocs = "SELECT * FROM documents WHERE application_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlDocs)) {
            pstmt.setInt(1, appId);
            try (ResultSet rsDoc = pstmt.executeQuery()) {
                while (rsDoc.next()) {
                    app.addDocument(new Document(
                            rsDoc.getInt("id"),
                            appId,
                            rsDoc.getString("document_type"),
                            rsDoc.getString("passport_number"),
                            rsDoc.getString("issuing_authority"),
                            rsDoc.getString("date_issued"),
                            rsDoc.getString("validity_date")
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