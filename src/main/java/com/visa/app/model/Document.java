package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Document
 *  OOP CONCEPT: Encapsulation
 * ============================================================
 *
 * Represents a supporting travel document linked to an application.
 * Normalized schema: documents (document_id, application_id, document_type).
 */
public class Document {
    private int id;
    private int applicationId;
    private String documentType;

    // Optional transient metadata used for UI presentation of attached documents
    private String passportNumber;
    private String issuingAuthority;
    private String dateIssued;
    private String validityDate;

    public Document() {
        this(-1, -1, "");
    }

    public Document(int id, int applicationId, String documentType) {
        this.id = id;
        this.applicationId = applicationId;
        this.documentType = documentType != null ? documentType.trim() : "";
    }

    public Document(String documentType) {
        this(-1, -1, documentType);
    }

    // Extended constructor for UI display when documents and passports are listed together
    public Document(int id, int applicationId, String documentType, String passportNumber,
                    String issuingAuthority, String dateIssued, String validityDate) {
        this(id, applicationId, documentType);
        this.passportNumber = passportNumber != null ? passportNumber : "";
        this.issuingAuthority = issuingAuthority != null ? issuingAuthority : "";
        this.dateIssued = dateIssued != null ? dateIssued : "";
        this.validityDate = validityDate != null ? validityDate : "";
    }

    public Document(String documentType, String passportNumber, String issuingAuthority,
                    String dateIssued, String validityDate) {
        this(-1, -1, documentType, passportNumber, issuingAuthority, dateIssued, validityDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getDocumentType() {
        return documentType != null ? documentType : "";
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType != null ? documentType.trim() : "";
    }

    public String getPassportNumber() {
        return passportNumber != null ? passportNumber : "";
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getIssuingAuthority() {
        return issuingAuthority != null ? issuingAuthority : "";
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getDateIssued() {
        return dateIssued != null ? dateIssued : "";
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getValidityDate() {
        return validityDate != null ? validityDate : "";
    }

    public void setValidityDate(String validityDate) {
        this.validityDate = validityDate;
    }

    public String getDocumentSummary() {
        return "Supporting Document: " + documentType;
    }

    @Override
    public String toString() {
        return getDocumentSummary();
    }
}
