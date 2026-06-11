package com.visa.app.utils;

import com.visa.app.dao.ApplicantDAO;
import com.visa.app.dao.ApplicationDAO;
import com.visa.app.dao.DocumentDAO;
import com.visa.app.model.Applicant;
import com.visa.app.model.Document;
import com.visa.app.model.Passport;
import com.visa.app.model.SupportingDocument;

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

    /** Q1: Insert a new applicant (user + application rows). */
    public boolean submitApplication(Applicant applicant, String password) {
        return applicantDAO.insertApplicant(applicant, password);
    }

    /** Q2: Load all applicants as OOP model objects. */
    public List<Applicant> getAllApplicants() {
        return applicantDAO.getAllApplicants();
    }

    /** Q3: Fetch one applicant with their email via JOIN. */
    public Applicant getApplicantWithEmail(int applicationId) {
        return applicantDAO.getApplicantWithEmail(applicationId);
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

    /** Q5: Search applications by keyword. */
    public List<Applicant> searchApplications(String keyword) {
        return applicationDAO.searchApplications(keyword);
    }

    /** Q6: Get each application with its document count (JOIN + COUNT). */
    public List<String[]> getApplicationsWithDocumentCount() {
        return applicationDAO.getApplicationsWithDocumentCount();
    }

    // ── Document operations ───────────────────────────────────────────────────

    /** Q7: Save a document (Passport or SupportingDocument). */
    public boolean saveDocument(Document document) {
        return documentDAO.insertDocument(document);
    }

    /** Convenience: save a passport document. */
    public boolean savePassport(int applicationId,
                                 String number, String authority,
                                 String dateIssued, String validityDate) {
        Passport p = new Passport(number, authority, dateIssued, validityDate);
        p.setApplicationId(applicationId);
        return documentDAO.insertDocument(p);
    }

    /** Convenience: save a supporting document (Air Ticket, etc.). */
    public boolean saveSupportingDocument(int applicationId, String type) {
        SupportingDocument sd = new SupportingDocument(type);
        sd.setApplicationId(applicationId);
        return documentDAO.insertDocument(sd);
    }

    /** Q8: Get all documents for an application. */
    public List<Document> getDocumentsForApplication(int applicationId) {
        return documentDAO.getDocumentsByApplicationId(applicationId);
    }

    /** Q9: 3-table JOIN — all applicants with passport details. */
    public List<String[]> getApplicantsWithPassportDetails() {
        return documentDAO.getApplicantsWithPassportDetails();
    }

    /** Q10: Subquery — only applications with all 4 document types. */
    public List<String[]> getCompleteApplications() {
        return documentDAO.getCompleteApplications();
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
