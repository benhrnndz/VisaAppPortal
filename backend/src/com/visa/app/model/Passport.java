package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Passport
 *  OOP CONCEPT: Encapsulation
 * ============================================================
 *
 * CORRECTED: Per the ERD, PassportT is its own standalone entity —
 * it is NOT a type of Document. The old version of this class
 * extended Document, which was wrong: a passport "belongs_to" an
 * Applicant and is "used_in"/"requires" an Application, but it is
 * never a row in the documents table.
 *
 * Maps to: `passports` table (passport_no, issued_by, date_of_issue, valid_until).
 */
public class Passport {

    // ── ENCAPSULATION: private fields, matching PassportT exactly ─────────────
    private String passportNo;     // PK
    private String issuedBy;
    private String dateOfIssue;    // YYYY/MM/DD
    private String validUntil;     // YYYY/MM/DD

    // ── Full constructor — used when loading a row from the `passports` table ──
    public Passport(String passportNo, String issuedBy, String dateOfIssue, String validUntil) {
        this.passportNo  = passportNo;
        this.issuedBy    = issuedBy;
        this.dateOfIssue = dateOfIssue;
        this.validUntil  = validUntil;
    }

    public String getDocumentSummary() {
        return "Passport #" + passportNo
             + " | Issued by: " + issuedBy
             + " | Date Issued: " + dateOfIssue
             + " | Valid until: " + validUntil;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public String getPassportNo()              { return passportNo; }
    public void   setPassportNo(String n)      { this.passportNo = n; }

    public String getIssuedBy()                { return issuedBy; }
    public void   setIssuedBy(String a)        { this.issuedBy = a; }

    public String getDateOfIssue()             { return dateOfIssue; }
    public void   setDateOfIssue(String d)     { this.dateOfIssue = d; }

    public String getValidUntil()              { return validUntil; }
    public void   setValidUntil(String v)      { this.validUntil = v; }

    @Override
    public String toString() { return getDocumentSummary(); }
}