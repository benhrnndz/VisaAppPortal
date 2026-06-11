package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Applicant
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * INHERITANCE  — `extends Person` means Applicant automatically gains
 *                firstName, lastName, dateOfBirth, getFullName(), and
 *                the toString() hook without re-declaring them.
 *                The constructor calls super(...) to initialise the
 *                parent's private fields through its constructor.
 *
 * POLYMORPHISM — Overrides getProfileSummary() so an Applicant prints
 *                differently from a Child when called through a Person ref.
 *
 * ENCAPSULATION — All applicant-specific fields are private with
 *                 public getters/setters.
 *
 * Maps to: `users` table (userId, email) + `applications` table (all other fields).
 */
public class Applicant extends Person {

    // ── ENCAPSULATION: private fields ─────────────────────────────────────────
    private int    userId;
    private int    applicationId;
    private String email;
    private String contactNumber;
    private String sex;
    private String citizenship;
    private String civilStatus;
    private String placeOfBirth;
    private String homeAddress;
    private String status;        // "PENDING" | "APPROVED" | "DENIED"

    // ── Full constructor (used when loading a record from the database) ────────
    public Applicant(int userId, int applicationId,
                     String firstName, String lastName, String dateOfBirth,
                     String email, String contactNumber,
                     String sex, String citizenship, String civilStatus,
                     String placeOfBirth, String homeAddress, String status) {
        super(firstName, lastName, dateOfBirth);   // INHERITANCE: calls Person(...)
        this.userId        = userId;
        this.applicationId = applicationId;
        this.email         = email;
        this.contactNumber = contactNumber;
        this.sex           = sex;
        this.citizenship   = citizenship;
        this.civilStatus   = civilStatus;
        this.placeOfBirth  = placeOfBirth;
        this.homeAddress   = homeAddress;
        this.status        = status;
    }

    /** Convenience constructor for a new submission (IDs assigned after DB insert). */
    public Applicant(String firstName, String lastName, String dateOfBirth,
                     String email, String contactNumber,
                     String sex, String citizenship, String civilStatus,
                     String placeOfBirth, String homeAddress) {
        this(-1, -1, firstName, lastName, dateOfBirth,
             email, contactNumber, sex, citizenship, civilStatus,
             placeOfBirth, homeAddress, "PENDING");
    }

    // ── POLYMORPHISM: override getProfileSummary() ────────────────────────────
    @Override
    public String getProfileSummary() {
        return "Primary Applicant: " + getFullName()
             + " | Citizenship: " + citizenship
             + " | Status: "      + status;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getUserId()                       { return userId; }
    public void   setUserId(int id)                 { this.userId = id; }

    public int    getApplicationId()                { return applicationId; }
    public void   setApplicationId(int id)          { this.applicationId = id; }

    public String getEmail()                        { return email; }
    public void   setEmail(String e)                { this.email = e; }

    public String getContactNumber()                { return contactNumber; }
    public void   setContactNumber(String n)        { this.contactNumber = n; }

    public String getSex()                          { return sex; }
    public void   setSex(String s)                  { this.sex = s; }

    public String getCitizenship()                  { return citizenship; }
    public void   setCitizenship(String c)          { this.citizenship = c; }

    public String getCivilStatus()                  { return civilStatus; }
    public void   setCivilStatus(String cs)         { this.civilStatus = cs; }

    public String getPlaceOfBirth()                 { return placeOfBirth; }
    public void   setPlaceOfBirth(String p)         { this.placeOfBirth = p; }

    public String getHomeAddress()                  { return homeAddress; }
    public void   setHomeAddress(String a)          { this.homeAddress = a; }

    public String getStatus()                       { return status; }
    public void   setStatus(String s)               { this.status = s; }

    public boolean isPending()  { return "PENDING".equalsIgnoreCase(status); }
    public boolean isApproved() { return "APPROVED".equalsIgnoreCase(status); }
    public boolean isDenied()   { return "DENIED".equalsIgnoreCase(status); }
}
