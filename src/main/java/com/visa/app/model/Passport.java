package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Passport
 *  OOP CONCEPT: Encapsulation
 * ============================================================
 *
 * Represents a national passport document record in the `passports` table.
 */
public class Passport {
    private String passportNo;  // Primary Key
    private String issuedBy;
    private String dateOfIssue; // YYYY/MM/DD
    private String validUntil;  // YYYY/MM/DD

    public Passport() {
        this("", "", "", "");
    }

    public Passport(String passportNo, String issuedBy, String dateOfIssue, String validUntil) {
        this.passportNo = passportNo != null ? passportNo.trim() : "";
        this.issuedBy = issuedBy != null ? issuedBy.trim() : "";
        this.dateOfIssue = dateOfIssue != null ? dateOfIssue.trim() : "";
        this.validUntil = validUntil != null ? validUntil.trim() : "";
    }

    public String getPassportNo() {
        return passportNo;
    }

    public void setPassportNo(String passportNo) {
        this.passportNo = passportNo != null ? passportNo.trim() : "";
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy != null ? issuedBy.trim() : "";
    }

    public String getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(String dateOfIssue) {
        this.dateOfIssue = dateOfIssue != null ? dateOfIssue.trim() : "";
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil != null ? validUntil.trim() : "";
    }

    public String getSummary() {
        return "Passport #" + passportNo
                + " | Issued by: " + issuedBy
                + " | Date Issued: " + dateOfIssue
                + " | Valid until: " + validUntil;
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
