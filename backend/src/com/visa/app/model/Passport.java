package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Passport
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * Extends Document and adds passport-specific fields.
 * Maps to `documents` rows where document_type = 'Original Passport'.
 */
public class Passport extends Document {

    // ── ENCAPSULATION: private passport-specific fields ───────────────────────
    private String passportNumber;
    private String issuingAuthority;
    private String dateIssued;    // YYYY/MM/DD
    private String validityDate;  // YYYY/MM/DD

    // ── Full constructor ──────────────────────────────────────────────────────
    public Passport(int id, int applicationId,
                    String passportNumber, String issuingAuthority,
                    String dateIssued, String validityDate) {
        super(id, applicationId, "Original Passport");   // INHERITANCE
        this.passportNumber   = passportNumber;
        this.issuingAuthority = issuingAuthority;
        this.dateIssued       = dateIssued;
        this.validityDate     = validityDate;
    }

    /** New passport not yet persisted. */
    public Passport(String passportNumber, String issuingAuthority,
                    String dateIssued, String validityDate) {
        this(-1, -1, passportNumber, issuingAuthority, dateIssued, validityDate);
    }

    // ── POLYMORPHISM: override getDocumentSummary() ───────────────────────────
    @Override
    public String getDocumentSummary() {
        return "Passport #" + passportNumber
             + " | Issued by: " + issuingAuthority
             + " | Date Issued: " + dateIssued
             + " | Valid until: " + validityDate;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public String getPassportNumber()               { return passportNumber; }
    public void   setPassportNumber(String n)       { this.passportNumber = n; }

    public String getIssuingAuthority()             { return issuingAuthority; }
    public void   setIssuingAuthority(String a)     { this.issuingAuthority = a; }

    public String getDateIssued()                   { return dateIssued; }
    public void   setDateIssued(String d)           { this.dateIssued = d; }

    public String getValidityDate()                 { return validityDate; }
    public void   setValidityDate(String v)         { this.validityDate = v; }
}
