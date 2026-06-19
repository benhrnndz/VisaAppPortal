package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Applicant
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * CORRECTED: Per the ERD, ApplicantT holds ONLY personal/biographic
 * data. Trip/visa-specific fields (status, passport_no, entry type,
 * length of stay, purpose, sponsor, etc.) belong to ApplicationT and
 * now live in the separate Application model — they used to be
 * incorrectly merged into this class.
 *
 * Maps to: `applicants` table (+ user_id FK -> `users`).
 */
public class Applicant extends Person {

    // ── ENCAPSULATION: private fields, matching ApplicantT exactly ────────────
    private int    applicantId;   // Applicant_ID (PK)
    private int    userId;        // FK -> users(id), for login
    private String sex;
    private String citizenship;
    private String contactNo;
    private String homeAddress;
    private String civilStatus;
    private String spouseName;
    private String occupation;
    private String employerOfficeAndAddress;
    private String fatherName;
    private String motherName;

    // ── Transient field — not stored in DB, used to pass email to DAO ─────────
    private String transientEmail = "";
    public String getTransientEmail()          { return transientEmail; }
    public void   setTransientEmail(String e)  { this.transientEmail = e; }

    // ── Full constructor (used when loading a record from the database) ────────
    public Applicant(int applicantId, int userId,
                     String firstName, String lastName, String dateOfBirth,
                     String placeOfBirth, String sex, String citizenship,
                     String contactNo, String homeAddress, String civilStatus,
                     String spouseName, String occupation, String employerOfficeAndAddress,
                     String fatherName, String motherName) {
        super(firstName, lastName, dateOfBirth);   // INHERITANCE: calls Person(...)
        this.applicantId = applicantId;
        this.userId       = userId;
        this.placeOfBirthInit(placeOfBirth);
        this.sex          = sex;
        this.citizenship  = citizenship;
        this.contactNo    = contactNo;
        this.homeAddress  = homeAddress;
        this.civilStatus  = civilStatus;
        this.spouseName   = spouseName;
        this.occupation   = occupation;
        this.employerOfficeAndAddress = employerOfficeAndAddress;
        this.fatherName   = fatherName;
        this.motherName   = motherName;
    }

    // place_of_birth isn't on Person, store it locally
    private String placeOfBirth;
    private void placeOfBirthInit(String p) { this.placeOfBirth = p; }
    public String getPlaceOfBirth()              { return placeOfBirth; }
    public void   setPlaceOfBirth(String p)      { this.placeOfBirth = p; }

    /** Convenience constructor for a brand-new submission (no IDs yet). */
    public Applicant(String firstName, String lastName, String dateOfBirth,
                     String placeOfBirth, String sex, String citizenship,
                     String contactNo, String homeAddress, String civilStatus) {
        this(-1, -1, firstName, lastName, dateOfBirth, placeOfBirth, sex, citizenship,
             contactNo, homeAddress, civilStatus, null, null, null, null, null);
    }

    // ── POLYMORPHISM: override getProfileSummary() ────────────────────────────
    @Override
    public String getProfileSummary() {
        return "Applicant: " + getFullName()
             + " | Citizenship: " + citizenship
             + " | Civil Status: " + civilStatus;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getApplicantId()                  { return applicantId; }
    public void   setApplicantId(int id)            { this.applicantId = id; }

    public int    getUserId()                       { return userId; }
    public void   setUserId(int id)                 { this.userId = id; }

    public String getSex()                          { return sex; }
    public void   setSex(String s)                  { this.sex = s; }

    public String getCitizenship()                  { return citizenship; }
    public void   setCitizenship(String c)          { this.citizenship = c; }

    public String getContactNo()                    { return contactNo; }
    public void   setContactNo(String n)            { this.contactNo = n; }

    public String getHomeAddress()                  { return homeAddress; }
    public void   setHomeAddress(String a)          { this.homeAddress = a; }

    public String getCivilStatus()                  { return civilStatus; }
    public void   setCivilStatus(String cs)         { this.civilStatus = cs; }

    public String getSpouseName()                   { return spouseName; }
    public void   setSpouseName(String s)           { this.spouseName = s; }

    public String getOccupation()                   { return occupation; }
    public void   setOccupation(String o)           { this.occupation = o; }

    public String getEmployerOfficeAndAddress()      { return employerOfficeAndAddress; }
    public void   setEmployerOfficeAndAddress(String e) { this.employerOfficeAndAddress = e; }

    public String getFatherName()                   { return fatherName; }
    public void   setFatherName(String f)           { this.fatherName = f; }

    public String getMotherName()                   { return motherName; }
    public void   setMotherName(String m)           { this.motherName = m; }
}