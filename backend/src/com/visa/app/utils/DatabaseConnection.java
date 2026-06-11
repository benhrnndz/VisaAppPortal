package com.visa.app.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ============================================================
 *  UTILITY: DatabaseConnection
 *  PATTERN:  Singleton
 * ============================================================
 *
 * The Singleton pattern ensures only ONE Connection object ever
 * exists for the application.  Without it, every DAO would open
 * its own connection, quickly exhausting the SQLite file lock.
 *
 * Usage (from any DAO):
 *   Connection conn = DatabaseConnection.getConnection();
 *
 * Points at the same visa_app.db file the frontend already uses,
 * so no separate setup is needed.
 */
public class DatabaseConnection {

    // ── Same DB path the frontend DatabaseManager uses ────────────────────────
    private static final String DB_URL = "jdbc:sqlite:visa_app.db";

    // ── Singleton instance — one per JVM lifetime ─────────────────────────────
    private static Connection connection = null;

    // ── Private constructor prevents `new DatabaseConnection()` ───────────────
    private DatabaseConnection() {}

    /**
     * Returns the shared Connection, creating it on first call.
     * synchronized keeps this thread-safe.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);
                System.out.println("[DatabaseConnection] Connected to visa_app.db");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Connection failed: " + e.getMessage());
        }
        return connection;
    }

    /** Cleanly close the connection — call on application shutdown. */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DatabaseConnection] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DatabaseConnection] Error closing: " + e.getMessage());
            }
        }
    }
}
