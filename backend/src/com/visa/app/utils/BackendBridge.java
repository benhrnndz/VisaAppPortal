package com.visa.app.utils;

import com.visa.app.dao.ApplicantDAO;
import com.visa.app.dao.ApplicationDAO;
import com.visa.app.dao.DocumentDAO;
import com.visa.app.model.Applicant;
import com.visa.app.model.Document;
import com.visa.app.model.Passport;

import java.util.List;

/**
 * ============================================================
 *  UTILITY: BackendBridge
 * ============================================================
 *
 * Single façade that the Swing UI panels call to reach the new
 * backend. Keeps all DAO instantiation in one place and provides
 * a clean API that the UI understands without importing DAO classes.
 *
 * Usage in any Swing ActionListener:
 *
 *   BackendBridge backend = BackendBridge.getInstance();
 *
 *   // Submit an application
 *   backend.submitApplication(applicant, password);
 *
 *   // Approve an application
 *   backend.approveApplication(appId);
 */
public class BackendBridge {

    private static BackendBridge instance;

    private final ApplicantDAO   applicantDAO;
    private final ApplicationDAO applicationDAO;
    private final DocumentDAO    documentDAO;

    private BackendBridge() {
        this.applicantDAO   = new ApplicantDAO();
        this.applicationDAO = new ApplicationDAO();
        this.documentDAO    = new DocumentDAO();
    }

    public static synchronized BackendBridge getInstance() {
        if (instance == null) instance = new BackendBridge();
        return instance;
    }

    // ── Applicant operations ──────────────────────────────────────────────────

    /** Q1: Insert a new applicant (users + applicants rows). */
    public boolean submitApplication(Applicant applicant, String email, String password) {
        return applicantDAO.insertApplicant(applicant, email, password);
    }

    /** Q1 convenience overload — email stored on the applicant object as a transient field. */
    public boolean submitApplication(Applicant applicant, String password) {
        // email is carried as a transient field set by the caller before this call
        return applicantDAO.insertApplicant(applicant, applicant.getTransientEmail(), password);
    }

    /** Q2: Load all applicants as OOP model objects. */
    public List<Applicant> getAllApplicants() {
        return applicantDAO.getAllApplicants();
    }

    // ── Application status operations ─────────────────────────────────────────

    /** Q4: Approve an application. */
    public boolean approveApplication(int applicationId) {
        return applicationDAO.updateApplicationStatus(applicationId, "APPROVED");
    }

    /** Q4 (reused): Deny an application. */
    public boolean denyApplication(int applicationId) {
        return applicationDAO.updateApplicationStatus(applicationId, "DENIED");
    }

    /** Q5: Search applications by keyword — returns rows for the admin table. */
    public List<Applicant> searchApplications(String keyword) {
        return applicationDAO.searchApplications(keyword);
    }

    /** Q5 variant: returns String[] rows (id, name, citizenship, status, doc_count) for the admin table. */
    public List<String[]> searchApplicationsAsRows(String keyword) {
        return applicationDAO.searchApplicationsAsRows(keyword);
    }

    /** Q6: Get each application with its document count (JOIN + COUNT). */
    public List<String[]> getApplicationsWithDocumentCount() {
        return applicationDAO.getApplicationsWithDocumentCount();
    }

    // ── Document operations ───────────────────────────────────────────────────

    /** Q7: Save a supporting document (Air Ticket, Invitation Letter, Bank Certificate). */
    public boolean saveDocument(Document document) {
        return documentDAO.insertDocument(document);
    }

    /** Convenience: save a passport to the passports table. */
    public boolean savePassport(String number, String authority,
                                String dateIssued, String validUntil) {
        return documentDAO.insertPassport(number, authority, dateIssued, validUntil);
    }

    /** Convenience: save a supporting document. */
    public boolean saveSupportingDocument(int applicationId, String type) {
        Document d = new Document(type);
        d.setApplicationId(applicationId);
        return documentDAO.insertDocument(d);
    }

    /** Q8: Get all documents for an application. */
    public List<Document> getDocumentsForApplication(int applicationId) {
        return documentDAO.getDocumentsByApplicationId(applicationId);
    }

    /** Q9: 3-table JOIN — all applicants with passport details. */
    public List<String[]> getApplicantsWithPassportDetails() {
        return documentDAO.getApplicantsWithPassportDetails();
    }

    /** Q3: JOIN + WHERE — fetch one applicant's full profile with their email. */
    public com.visa.app.model.Applicant getApplicantProfile(int applicantId) {
        return applicantDAO.getApplicantProfile(applicantId);
    }

    /** Q10: Subquery — only applications with all 3 supporting doc types. */
    public List<String[]> getCompleteApplications() {
        return documentDAO.getCompleteApplications();
    }

    /** Q11: Correlated subquery — applications with passports expiring within 180 days. */
    public List<String[]> getApplicationsWithExpiringPassports() {
        return documentDAO.getApplicationsWithExpiringPassports();
    }

    // ── Polymorphism demo helper ───────────────────────────────────────────────

    /**
     * Demonstrates runtime polymorphism in a printable format.
     * Pass any List<? extends Person> or List<? extends Document> and each
     * object's overridden getProfileSummary() / getDocumentSummary() fires.
     */
    public void printProfileSummaries(List<? extends com.visa.app.model.Person> people) {
        System.out.println("=== Profile Summaries (Polymorphism demo) ===");
        for (com.visa.app.model.Person p : people) {
            System.out.println(p.getProfileSummary());   // dispatches to correct subclass
        }
    }
}