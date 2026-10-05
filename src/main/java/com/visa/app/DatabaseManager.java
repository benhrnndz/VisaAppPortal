package com.visa.app;

import com.visa.app.dao.DatabaseConnection;
import com.visa.app.model.User;
import com.visa.app.model.VisaApplication;
import com.visa.app.service.VisaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================
 *  FACADE: DatabaseManager
 * ============================================================
 *
 * Backwards-compatible adapter for existing UI components.
 * Delegates all persistence operations to the unified VisaService
 * and DatabaseConnection.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private final VisaService visaService;

    private DatabaseManager() {
        DatabaseConnection.initializeDatabase();
        this.visaService = VisaService.getInstance();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public boolean registerUser(String email, String password, String role) {
        return visaService.registerUser(email, password, role);
    }

    public User loginUser(String email, String password) {
        return visaService.loginUser(email, password);
    }

    public boolean saveApplication(VisaApplication app) {
        return visaService.saveVisaApplication(app);
    }

    public boolean updateApplication(VisaApplication app) {
        return visaService.updateVisaApplication(app);
    }

    public boolean deleteApplication(int appId) {
        return visaService.deleteApplication(appId);
    }

    public boolean updateApplicationStatus(int appId, String status) {
        return visaService.updateApplicationStatus(appId, status);
    }

    public List<VisaApplication> getApplicationsByUserId(int userId) {
        return visaService.getVisaApplicationsByUserId(userId);
    }

    public List<VisaApplication> getAllApplications() {
        return visaService.getAllVisaApplications();
    }

    public List<VisaApplication> searchApplications(String query) {
        return visaService.searchVisaApplications(query);
    }
}
