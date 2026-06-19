package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Application   (NEW — previously missing)
 *  OOP CONCEPT: Encapsulation, Composition
 * ============================================================
 *
 * Per the ERD, ApplicationT is its own entity, related to ApplicantT
 * (one applicant submits many applications) and to PassportT
 * (one passport used_in/requires one application). The old code had
 * NO model for this table at all — its fields were wrongly bolted
 * onto Applicant. This class restores it.
 *
 * Maps to: `applications` table.
 */
public class Application {

    // ── ENCAPSULATION: private fields, matching ApplicationT exactly ──────────
    private int    applicationId;        // Application_ID (PK)
    private int    applicantId;          // Applicant_ID (FK)
    private String passportNo;           // Passport_No (FK)
    private String requestedEntryType;
    private int    lengthOfStayDays;
    private String portOfEntry;
    private String destAfterPH;
    private int    ageUponApplication;
    private String dateOfApplication;
    private String purposeType;
    private String sponsorName;
    private String sponContactNo;
    private String status;               // "PENDING" | "APPROVED" | "DENIED"

    public Application(int applicationId, int applicantId, String passportNo,
                        String requestedEntryType, int lengthOfStayDays, String portOfEntry,
                        String destAfterPH, int ageUponApplication, String dateOfApplication,
                        String purposeType, String sponsorName, String sponContactNo,
                        String status) {
        this.applicationId       = applicationId;
        this.applicantId         = applicantId;
        this.passportNo          = passportNo;
        this.requestedEntryType  = requestedEntryType;
        this.lengthOfStayDays    = lengthOfStayDays;
        this.portOfEntry         = portOfEntry;
        this.destAfterPH         = destAfterPH;
        this.ageUponApplication  = ageUponApplication;
        this.dateOfApplication   = dateOfApplication;
        this.purposeType         = purposeType;
        this.sponsorName         = sponsorName;
        this.sponContactNo       = sponContactNo;
        this.status              = status;
    }

    public String getSummary() {
        return "Application #" + applicationId
             + " | Purpose: " + purposeType
             + " | Status: " + status;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getApplicationId()                { return applicationId; }
    public void   setApplicationId(int id)          { this.applicationId = id; }

    public int    getApplicantId()                  { return applicantId; }
    public void   setApplicantId(int id)            { this.applicantId = id; }

    public String getPassportNo()                   { return passportNo; }
    public void   setPassportNo(String p)           { this.passportNo = p; }

    public String getRequestedEntryType()            { return requestedEntryType; }
    public void   setRequestedEntryType(String t)    { this.requestedEntryType = t; }

    public int    getLengthOfStayDays()              { return lengthOfStayDays; }
    public void   setLengthOfStayDays(int d)         { this.lengthOfStayDays = d; }

    public String getPortOfEntry()                   { return portOfEntry; }
    public void   setPortOfEntry(String p)           { this.portOfEntry = p; }

    public String getDestAfterPH()                   { return destAfterPH; }
    public void   setDestAfterPH(String d)           { this.destAfterPH = d; }

    public int    getAgeUponApplication()             { return ageUponApplication; }
    public void   setAgeUponApplication(int a)        { this.ageUponApplication = a; }

    public String getDateOfApplication()              { return dateOfApplication; }
    public void   setDateOfApplication(String d)      { this.dateOfApplication = d; }

    public String getPurposeType()                    { return purposeType; }
    public void   setPurposeType(String p)            { this.purposeType = p; }

    public String getSponsorName()                    { return sponsorName; }
    public void   setSponsorName(String s)            { this.sponsorName = s; }

    public String getSponContactNo()                  { return sponContactNo; }
    public void   setSponContactNo(String s)          { this.sponContactNo = s; }

    public String getStatus()                         { return status; }
    public void   setStatus(String s)                 { this.status = s; }

    public boolean isPending()  { return "PENDING".equalsIgnoreCase(status); }
    public boolean isApproved() { return "APPROVED".equalsIgnoreCase(status); }
    public boolean isDenied()   { return "DENIED".equalsIgnoreCase(status); }

    @Override
    public String toString() { return getSummary(); }
}