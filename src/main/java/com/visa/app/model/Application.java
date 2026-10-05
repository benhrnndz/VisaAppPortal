package com.visa.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  MODEL: Application
 *  OOP CONCEPT: Encapsulation + Composition
 * ============================================================
 *
 * Travel visa application record.
 * Maps to: `applications` table.
 * References:
 *   - applicant_id -> applicants(applicant_id)
 *   - passport_no  -> passports(passport_no)
 */
public class Application {

    private int applicationId;
    private int applicantId;
    private String passportNo;
    private String requestedEntryType;
    private int lengthOfStayDays;
    private String portOfEntry;
    private String destAfterPH;
    private int ageUponApplication;
    private String dateOfApplication;
    private String purposeType;
    private String sponsorName;
    private String sponContactNo;
    private ApplicationStatus status;

    // Composition references
    private Applicant applicant;
    private Passport passport;
    private List<Child> children;
    private List<Document> documents;

    public Application() {
        this(-1, -1, "", "Single", 30, "NAIA", "", 0, "", "Tourism", "", "", ApplicationStatus.PENDING);
    }

    public Application(int applicationId, int applicantId, String passportNo,
                       String requestedEntryType, int lengthOfStayDays, String portOfEntry,
                       String destAfterPH, int ageUponApplication, String dateOfApplication,
                       String purposeType, String sponsorName, String sponContactNo,
                       ApplicationStatus status) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.passportNo = passportNo != null ? passportNo.trim() : "";
        this.requestedEntryType = requestedEntryType != null ? requestedEntryType.trim() : "Single";
        this.lengthOfStayDays = Math.max(1, lengthOfStayDays);
        this.portOfEntry = portOfEntry != null ? portOfEntry.trim() : "NAIA";
        this.destAfterPH = destAfterPH != null ? destAfterPH.trim() : "";
        this.ageUponApplication = Math.max(0, ageUponApplication);
        this.dateOfApplication = dateOfApplication != null ? dateOfApplication.trim() : "";
        this.purposeType = purposeType != null ? purposeType.trim() : "Tourism";
        this.sponsorName = sponsorName != null ? sponsorName.trim() : "";
        this.sponContactNo = sponContactNo != null ? sponContactNo.trim() : "";
        this.status = status != null ? status : ApplicationStatus.PENDING;

        this.children = new ArrayList<>();
        this.documents = new ArrayList<>();
    }

    public Application(int applicationId, int applicantId, String passportNo,
                       String requestedEntryType, int lengthOfStayDays, String portOfEntry,
                       String destAfterPH, int ageUponApplication, String dateOfApplication,
                       String purposeType, String sponsorName, String sponContactNo,
                       String statusStr) {
        this(applicationId, applicantId, passportNo, requestedEntryType, lengthOfStayDays,
             portOfEntry, destAfterPH, ageUponApplication, dateOfApplication, purposeType,
             sponsorName, sponContactNo, ApplicationStatus.fromString(statusStr));
    }

    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }

    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }

    public boolean isDenied() {
        return status == ApplicationStatus.DENIED;
    }

    public String getSummary() {
        return "Application #" + applicationId
                + " | Purpose: " + purposeType
                + " | Status: " + status;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int id) { this.applicationId = id; }

    // Compatibility getter/setter for code calling getId()
    public int getId() { return applicationId; }
    public void setId(int id) { this.applicationId = id; }

    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int id) { this.applicantId = id; }

    public String getPassportNo() { return passportNo; }
    public void setPassportNo(String passportNo) { this.passportNo = passportNo != null ? passportNo : ""; }

    public String getRequestedEntryType() { return requestedEntryType; }
    public void setRequestedEntryType(String t) { this.requestedEntryType = t != null ? t : ""; }

    public int getLengthOfStayDays() { return lengthOfStayDays; }
    public void setLengthOfStayDays(int days) { this.lengthOfStayDays = Math.max(1, days); }

    public String getPortOfEntry() { return portOfEntry; }
    public void setPortOfEntry(String portOfEntry) { this.portOfEntry = portOfEntry != null ? portOfEntry : ""; }

    public String getDestAfterPH() { return destAfterPH; }
    public void setDestAfterPH(String dest) { this.destAfterPH = dest != null ? dest : ""; }

    public int getAgeUponApplication() { return ageUponApplication; }
    public void setAgeUponApplication(int age) { this.ageUponApplication = Math.max(0, age); }

    public String getDateOfApplication() { return dateOfApplication; }
    public void setDateOfApplication(String date) { this.dateOfApplication = date != null ? date : ""; }

    public String getPurposeType() { return purposeType; }
    public void setPurposeType(String purpose) { this.purposeType = purpose != null ? purpose : ""; }

    public String getSponsorName() { return sponsorName; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName != null ? sponsorName : ""; }

    public String getSponContactNo() { return sponContactNo; }
    public void setSponContactNo(String contact) { this.sponContactNo = contact != null ? contact : ""; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status != null ? status : ApplicationStatus.PENDING; }

    public void setStatus(String statusStr) {
        this.status = ApplicationStatus.fromString(statusStr);
    }

    public Applicant getApplicant() { return applicant; }
    public void setApplicant(Applicant applicant) { this.applicant = applicant; }

    public Passport getPassport() { return passport; }
    public void setPassport(Passport passport) { this.passport = passport; }

    public List<Child> getChildren() { return children; }
    public void setChildren(List<Child> children) { this.children = children != null ? children : new ArrayList<>(); }
    public void addChild(Child child) { if (child != null) this.children.add(child); }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents != null ? documents : new ArrayList<>(); }
    public void addDocument(Document doc) { if (doc != null) this.documents.add(doc); }

    @Override
    public String toString() {
        return getSummary();
    }
}
