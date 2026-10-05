package com.visa.app.service;

import com.visa.app.dao.ApplicantDAO;
import com.visa.app.dao.ApplicationDAO;
import com.visa.app.dao.ChildDAO;
import com.visa.app.dao.DatabaseConnection;
import com.visa.app.dao.DocumentDAO;
import com.visa.app.dao.PassportDAO;
import com.visa.app.dao.UserDAO;
import com.visa.app.model.Applicant;
import com.visa.app.model.Application;
import com.visa.app.model.ApplicationStatus;
import com.visa.app.model.ApplicationSummaryDTO;
import com.visa.app.model.Child;
import com.visa.app.model.Document;
import com.visa.app.model.Passport;
import com.visa.app.model.Person;
import com.visa.app.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ============================================================
 *  SERVICE: VisaService
 *  PATTERN: Facade + Transaction Orchestration
 * ============================================================
 *
 * Central business service for the Visa Application Portal.
 * Handles atomic cross-entity transactions, validation, security,
 * and reporting queries.
 */
public class VisaService {

    private static final Logger LOGGER = Logger.getLogger(VisaService.class.getName());
    private static VisaService instance;

    private final UserDAO userDAO;
    private final ApplicantDAO applicantDAO;
    private final ApplicationDAO applicationDAO;
    private final PassportDAO passportDAO;
    private final DocumentDAO documentDAO;
    private final ChildDAO childDAO;

    public VisaService() {
        this(new UserDAO(), new ApplicantDAO(), new ApplicationDAO(),
             new PassportDAO(), new DocumentDAO(), new ChildDAO());
    }

    public VisaService(UserDAO userDAO, ApplicantDAO applicantDAO, ApplicationDAO applicationDAO,
                       PassportDAO passportDAO, DocumentDAO documentDAO, ChildDAO childDAO) {
        this.userDAO = userDAO;
        this.applicantDAO = applicantDAO;
        this.applicationDAO = applicationDAO;
        this.passportDAO = passportDAO;
        this.documentDAO = documentDAO;
        this.childDAO = childDAO;
    }

    public static synchronized VisaService getInstance() {
        if (instance == null) {
            instance = new VisaService();
        }
        return instance;
    }

    // ── Authentication Operations ─────────────────────────────────────────────

    public boolean registerUser(String email, String password, String role) {
        return userDAO.registerUser(email, password, role);
    }

    public User loginUser(String email, String password) {
        return userDAO.loginUser(email, password);
    }

    // ── Full Application Submission & CRUD (Transactional) ───────────────────

    /**
     * Atomically saves an entire visa application and its associated entity graph:
     * 1. Resolve or create Applicant profile
     * 2. Insert/upsert Passport record
     * 3. Insert Application travel record
     * 4. Insert Children dependents
     * 5. Insert supporting Documents
     */
    public boolean saveApplication(Application app) {
        if (app == null || app.getApplicant() == null) {
            LOGGER.warning("Cannot save null application or application without applicant.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Applicant applicant = app.getApplicant();

            // Step 1: Ensure applicant row exists
            Integer existingApplicantId = applicantDAO.findApplicantIdByUserId(applicant.getUserId(), conn);
            int applicantId;
            if (existingApplicantId == null || existingApplicantId <= 0) {
                applicantId = applicantDAO.insertApplicant(applicant, conn);
                if (applicantId <= 0) {
                    throw new SQLException("Failed to create applicant record.");
                }
            } else {
                applicantId = existingApplicantId;
                applicant.setApplicantId(applicantId);
                applicantDAO.updateApplicant(applicant, conn);
            }
            app.setApplicantId(applicantId);

            // Step 2: Ensure passport row exists
            String passportNo = (app.getPassport() != null && !app.getPassport().getPassportNo().isBlank())
                    ? app.getPassport().getPassportNo()
                    : "N/A-" + applicant.getUserId();
            app.setPassportNo(passportNo);

            Passport passport = app.getPassport() != null
                    ? app.getPassport()
                    : new Passport(passportNo, "Pending", "2020/01/01", "2030/01/01");
            passportDAO.insertOrUpdatePassport(passport);

            // Step 3: Insert Application
            int appId = applicationDAO.insertApplication(app, conn);
            app.setApplicationId(appId);

            // Step 4: Insert Children
            if (app.getChildren() != null && !app.getChildren().isEmpty()) {
                for (Child child : app.getChildren()) {
                    child.setApplicantId(applicantId);
                    childDAO.insertChild(child, conn);
                }
            }

            // Step 5: Insert Supporting Documents
            if (app.getDocuments() != null && !app.getDocuments().isEmpty()) {
                for (Document doc : app.getDocuments()) {
                    if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())) {
                        continue; // Passports live in passports table
                    }
                    doc.setApplicationId(appId);
                    documentDAO.insertDocument(doc, conn);
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            LOGGER.log(Level.INFO, "Application #{0} saved successfully (applicant_id={1}).",
                    new Object[]{appId, applicantId});
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Transaction rollback while saving application: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Atomically updates an existing visa application and its dependents.
     */
    public boolean updateApplication(Application app) {
        if (app == null || app.getApplicationId() <= 0) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Applicant applicant = app.getApplicant();
            if (applicant != null && applicant.getApplicantId() > 0) {
                applicantDAO.updateApplicant(applicant, conn);
            }

            if (app.getPassport() != null && !app.getPassport().getPassportNo().isBlank()) {
                passportDAO.insertOrUpdatePassport(app.getPassport());
            }

            applicationDAO.updateApplication(app, conn);

            int applicantId = app.getApplicantId();
            if (applicantId > 0) {
                childDAO.deleteChildrenByApplicantId(applicantId, conn);
                if (app.getChildren() != null) {
                    for (Child child : app.getChildren()) {
                        child.setApplicantId(applicantId);
                        childDAO.insertChild(child, conn);
                    }
                }
            }

            documentDAO.deleteDocumentsByApplicationId(app.getApplicationId(), conn);
            if (app.getDocuments() != null) {
                for (Document doc : app.getDocuments()) {
                    if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())) continue;
                    doc.setApplicationId(app.getApplicationId());
                    documentDAO.insertDocument(doc, conn);
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            LOGGER.log(Level.INFO, "Application #{0} updated successfully.", app.getApplicationId());
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            LOGGER.log(Level.SEVERE, "Transaction rollback while updating application: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteApplication(int applicationId) {
        return applicationDAO.deleteApplication(applicationId);
    }

    // ── Application Status Operations (Q4) ────────────────────────────────────

    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        return applicationDAO.updateApplicationStatus(applicationId, newStatus);
    }

    public boolean approveApplication(int applicationId) {
        return applicationDAO.updateApplicationStatus(applicationId, ApplicationStatus.APPROVED.getValue());
    }

    public boolean denyApplication(int applicationId) {
        return applicationDAO.updateApplicationStatus(applicationId, ApplicationStatus.DENIED.getValue());
    }

    // ── Queries & Reports (Q1 - Q11) ──────────────────────────────────────────

    /** Q1: Insert new applicant and user login */
    public boolean submitApplicant(Applicant applicant, String email, String password) {
        return applicantDAO.insertApplicant(applicant, email, password);
    }

    /** Q2: Select all applicants */
    public List<Applicant> getAllApplicants() {
        return applicantDAO.getAllApplicants();
    }

    /** Q3: Applicant profile with email via JOIN + WHERE */
    public Applicant getApplicantProfile(int applicantId) {
        return applicantDAO.getApplicantProfile(applicantId);
    }

    /** Q5: Search applications by applicant name or citizenship */
    public List<Applicant> searchApplications(String keyword) {
        return applicationDAO.searchApplications(keyword);
    }

    public List<ApplicationSummaryDTO> searchApplicationsAsDTOs(String keyword) {
        return applicationDAO.searchApplicationsAsDTOs(keyword);
    }

    public List<String[]> searchApplicationsAsRows(String keyword) {
        return applicationDAO.searchApplicationsAsRows(keyword);
    }

    /** Q6: Applications with document count (JOIN + GROUP BY + COUNT) */
    public List<String[]> getApplicationsWithDocumentCount() {
        return applicationDAO.getApplicationsWithDocumentCount();
    }

    /** Q7: Save supporting document */
    public boolean saveDocument(Document document) {
        return documentDAO.insertDocument(document);
    }

    /** Q8: Get all documents for an application */
    public List<Document> getDocumentsForApplication(int applicationId) {
        return documentDAO.getDocumentsByApplicationId(applicationId);
    }

    /** Q9: 3-table JOIN (users -> applicants -> applications -> passports) */
    public List<String[]> getApplicantsWithPassportDetails() {
        return documentDAO.getApplicantsWithPassportDetails();
    }

    /** Q10: Subquery + HAVING (applications with all 3 supporting docs) */
    public List<String[]> getCompleteApplications() {
        return documentDAO.getCompleteApplications();
    }

    /** Q11: Correlated subquery (applications with expiring passports) */
    public List<String[]> getApplicationsWithExpiringPassports() {
        return documentDAO.getApplicationsWithExpiringPassports();
    }

    public List<Application> getApplicationsByUserId(int userId) {
        List<Application> apps = applicationDAO.getApplicationsByUserId(userId);
        populateApplicationDetails(apps);
        return apps;
    }

    public List<Application> getAllApplications() {
        List<Application> apps = applicationDAO.getAllApplications();
        populateApplicationDetails(apps);
        return apps;
    }

    // ── VisaApplication ViewModel Methods for UI ─────────────────────────────

    public boolean saveVisaApplication(com.visa.app.model.VisaApplication va) {
        if (va == null) return false;
        Application domainApp = va.toDomainApplication();
        boolean success = saveApplication(domainApp);
        if (success) {
            va.setId(domainApp.getApplicationId());
        }
        return success;
    }

    public boolean updateVisaApplication(com.visa.app.model.VisaApplication va) {
        if (va == null) return false;
        return updateApplication(va.toDomainApplication());
    }

    public List<com.visa.app.model.VisaApplication> getVisaApplicationsByUserId(int userId) {
        List<Application> apps = getApplicationsByUserId(userId);
        List<com.visa.app.model.VisaApplication> list = new java.util.ArrayList<>(apps.size());
        for (Application app : apps) {
            list.add(com.visa.app.model.VisaApplication.fromDomainApplication(app));
        }
        return list;
    }

    public List<com.visa.app.model.VisaApplication> getAllVisaApplications() {
        List<Application> apps = getAllApplications();
        List<com.visa.app.model.VisaApplication> list = new java.util.ArrayList<>(apps.size());
        for (Application app : apps) {
            list.add(com.visa.app.model.VisaApplication.fromDomainApplication(app));
        }
        return list;
    }

    public List<com.visa.app.model.VisaApplication> searchVisaApplications(String query) {
        List<com.visa.app.model.VisaApplication> all = getAllVisaApplications();
        if (query == null || query.isBlank()) return all;
        String q = query.trim().toLowerCase();
        List<com.visa.app.model.VisaApplication> filtered = new java.util.ArrayList<>();
        for (com.visa.app.model.VisaApplication va : all) {
            if (va.getFullName().toLowerCase().contains(q)
                    || va.getEmail().toLowerCase().contains(q)
                    || va.getStatus().toLowerCase().contains(q)
                    || va.getCitizenship().toLowerCase().contains(q)) {
                filtered.add(va);
            }
        }
        return filtered;
    }

    private void populateApplicationDetails(List<Application> apps) {
        for (Application app : apps) {
            if (app.getApplicantId() > 0) {
                app.setChildren(childDAO.getChildrenByApplicantId(app.getApplicantId()));
            }
            if (app.getApplicationId() > 0) {
                app.setDocuments(documentDAO.getDocumentsByApplicationId(app.getApplicationId()));
            }
            if (app.getPassportNo() != null && !app.getPassportNo().isBlank() && !app.getPassportNo().startsWith("N/A-")) {
                Passport p = passportDAO.getPassportByNumber(app.getPassportNo());
                app.setPassport(p);
            }
        }
    }

    // ── Polymorphism Demonstration ─────────────────────────────────────────────

    public void printProfileSummaries(List<? extends Person> people) {
        if (people == null) return;
        System.out.println("=== Profile Summaries (Polymorphism Demo) ===");
        for (Person p : people) {
            System.out.println(p.getProfileSummary());
        }
    }
}
