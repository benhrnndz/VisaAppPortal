package com.visa.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ============================================================
 *  UTILITY: DatabaseConnection
 *  PATTERN: Singleton with connection lifecycle management
 * ============================================================
 *
 * Provides thread-safe, centralized SQLite connection management.
 * Enforces PRAGMA foreign_keys = ON on every connection to ensure
 * referential integrity and ON DELETE CASCADE behavior.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static String dbUrl = "jdbc:sqlite:visa_app.db";
    private static Connection connection = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found on classpath", e);
        }

        // Register JVM shutdown hook to cleanly close the connection
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::close, "db-shutdown-hook"));
    }

    private DatabaseConnection() {}

    /**
     * Overrides the SQLite database URL (useful for integration tests with in-memory DB).
     */
    public static synchronized void setDbUrl(String newUrl) {
        if (newUrl != null && !newUrl.equals(dbUrl)) {
            close();
            dbUrl = newUrl;
        }
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    /**
     * Returns the singleton Connection, creating and configuring it if necessary.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            LOGGER.log(Level.FINE, "Connected to SQLite database: {0} (foreign_keys=ON)", dbUrl);
        }
        return connection;
    }

    /**
     * Initializes the normalized 6-table SQLite schema and seeds default users.
     */
    public static synchronized void initializeDatabase() {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");

                // Table 1: users
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "email TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL," +
                        "role TEXT NOT NULL" +
                        ");");

                // Table 2: passports
                stmt.execute("CREATE TABLE IF NOT EXISTS passports (" +
                        "passport_no TEXT PRIMARY KEY," +
                        "issued_by TEXT NOT NULL," +
                        "date_of_issue TEXT NOT NULL," +
                        "valid_until TEXT NOT NULL" +
                        ");");

                // Table 3: applicants
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

                // Table 4: applications
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

                // Table 5: children
                stmt.execute("CREATE TABLE IF NOT EXISTS children (" +
                        "child_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "applicant_id INTEGER NOT NULL," +
                        "child_name TEXT NOT NULL," +
                        "child_age INTEGER NOT NULL," +
                        "FOREIGN KEY(applicant_id) REFERENCES applicants(applicant_id) ON DELETE CASCADE" +
                        ");");

                // Table 6: documents
                stmt.execute("CREATE TABLE IF NOT EXISTS documents (" +
                        "document_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "application_id INTEGER NOT NULL," +
                        "document_type TEXT NOT NULL," +
                        "FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE" +
                        ");");

                // Performance Indices
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_applicants_user_id ON applicants(user_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_applications_applicant_id ON applications(applicant_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_applications_passport_no ON applications(passport_no);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_children_applicant_id ON children(applicant_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_documents_application_id ON documents(application_id);");
            }

            seedDefaultUsers(conn);
            LOGGER.log(Level.INFO, "Database schema initialized successfully.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
        }
    }

    private static void seedDefaultUsers(Connection conn) {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE email = 'admin@visa.com'";
        String insertAdmin = "INSERT INTO users (email, password, role) VALUES ('admin@visa.com', 'admin123', 'ADMIN')";
        String checkUser = "SELECT COUNT(*) FROM users WHERE email = 'user@visa.com'";
        String insertUser = "INSERT INTO users (email, password, role) VALUES ('user@visa.com', 'user123', 'APPLICANT')";

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(insertAdmin);
                    LOGGER.info("Default Admin seeded (admin@visa.com)");
                }
            }
            try (ResultSet rs = stmt.executeQuery(checkUser)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(insertUser);
                    LOGGER.info("Default Applicant seeded (user@visa.com)");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error seeding default users: {0}", e.getMessage());
        }
    }

    /**
     * Closes the active connection cleanly.
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                LOGGER.log(Level.FINE, "Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error while closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
