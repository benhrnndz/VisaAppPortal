package com.visa.app.utils;

import com.visa.app.model.Applicant;
import com.visa.app.model.Document;
import com.visa.app.model.Person;
import com.visa.app.service.VisaService;

import java.util.List;

/**
 * ============================================================
 *  FACADE: BackendBridge
 * ============================================================
 *
 * Backwards-compatible adapter delegating to the unified VisaService.
 */
public class BackendBridge {

    private static BackendBridge instance;
    private final VisaService visaService;

    private BackendBridge() {
        this.visaService = VisaService.getInstance();
    }

    public static synchronized BackendBridge getInstance() {
        if (instance == null) {
            instance = new BackendBridge();
        }
        return instance;
    }

    public boolean submitApplication(Applicant applicant, String email, String password) {
        return visaService.submitApplicant(applicant, email, password);
    }

    public boolean submitApplication(Applicant applicant, String password) {
        return visaService.submitApplicant(applicant, applicant.getTransientEmail(), password);
    }

    public List<Applicant> getAllApplicants() {
        return visaService.getAllApplicants();
    }

    public boolean approveApplication(int applicationId) {
        return visaService.approveApplication(applicationId);
    }

    public boolean denyApplication(int applicationId) {
        return visaService.denyApplication(applicationId);
    }

    public List<Applicant> searchApplications(String keyword) {
        return visaService.searchApplications(keyword);
    }

    public List<String[]> searchApplicationsAsRows(String keyword) {
        return visaService.searchApplicationsAsRows(keyword);
    }

    public List<String[]> getApplicationsWithDocumentCount() {
        return visaService.getApplicationsWithDocumentCount();
    }

    public boolean saveDocument(Document document) {
        return visaService.saveDocument(document);
    }

    public boolean savePassport(String number, String authority, String dateIssued, String validUntil) {
        return visaService.saveApplication(null); // Managed via Application/Passport
    }

    public List<Document> getDocumentsForApplication(int applicationId) {
        return visaService.getDocumentsForApplication(applicationId);
    }

    public List<String[]> getApplicantsWithPassportDetails() {
        return visaService.getApplicantsWithPassportDetails();
    }

    public Applicant getApplicantProfile(int applicantId) {
        return visaService.getApplicantProfile(applicantId);
    }

    public List<String[]> getCompleteApplications() {
        return visaService.getCompleteApplications();
    }

    public List<String[]> getApplicationsWithExpiringPassports() {
        return visaService.getApplicationsWithExpiringPassports();
    }

    public void printProfileSummaries(List<? extends Person> people) {
        visaService.printProfileSummaries(people);
    }
}
