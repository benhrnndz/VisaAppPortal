package com.visa.app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use the native system look and feel for a cleaner interface
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not load system LookAndFeel, defaulting to Java L&F: " + e.getMessage());
        }

        // Initialize Database Connection and Schema
        SwingUtilities.invokeLater(() -> {
            try {
                // Instantiating DatabaseManager initializes SQLite connection and seeds default accounts
                DatabaseManager db = DatabaseManager.getInstance();
                System.out.println("Database initialization completed.");
                
                // Show Login Screen
                LoginScreen login = new LoginScreen();
                login.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "Database Initialization Error:\n" + e.getMessage() + "\n\nPlease check SQLite driver configuration.", 
                        "Critical Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
