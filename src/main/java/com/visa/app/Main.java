package com.visa.app;

import com.visa.app.dao.DatabaseConnection;
import com.visa.app.ui.screen.LoginScreen;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not load system LookAndFeel, defaulting to Java L&F: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize SQLite connection and schema tables if not present
                DatabaseConnection.initializeDatabase();
                System.out.println("Database initialization completed successfully.");

                // Show Login Screen
                LoginScreen login = new LoginScreen();
                login.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Database Initialization Error:\n" + e.getMessage() + "\n\nPlease check SQLite configuration.",
                        "Critical Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
