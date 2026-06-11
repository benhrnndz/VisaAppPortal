package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: SupportingDocument
 *  OOP CONCEPT: Inheritance + Polymorphism
 * ============================================================
 *
 * Concrete Document subclass for Air Ticket, Invitation Letter,
 * and Bank Certificate — documents that have no extra fields.
 * Maps to `documents` rows where document_type != 'Original Passport'.
 */
public class SupportingDocument extends Document {

    // ── Constructor ───────────────────────────────────────────────────────────
    public SupportingDocument(int id, int applicationId, String documentType) {
        super(id, applicationId, documentType);   // INHERITANCE
    }

    public SupportingDocument(String documentType) {
        this(-1, -1, documentType);
    }

    // ── POLYMORPHISM: override getDocumentSummary() ───────────────────────────
    @Override
    public String getDocumentSummary() {
        return "Supporting Document: " + getDocumentType();
    }
}
