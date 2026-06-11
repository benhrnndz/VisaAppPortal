package com.visa.app.model;

/**
 * ============================================================
 *  ABSTRACT BASE CLASS: Document
 *  OOP CONCEPT: Abstraction + Encapsulation + Polymorphism
 * ============================================================
 *
 * Second inheritance hierarchy in the project (alongside Person).
 * Passport and SupportingDocument extend this class.
 *
 * Maps to: `documents` table.
 */
public abstract class Document {

    // ── ENCAPSULATION: private shared fields ──────────────────────────────────
    private int    id;
    private int    applicationId;
    private String documentType;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Document(int id, int applicationId, String documentType) {
        this.id            = id;
        this.applicationId = applicationId;
        this.documentType  = documentType;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getId()                       { return id; }
    public void   setId(int id)                 { this.id = id; }

    public int    getApplicationId()            { return applicationId; }
    public void   setApplicationId(int appId)   { this.applicationId = appId; }

    public String getDocumentType()             { return documentType; }
    public void   setDocumentType(String dt)    { this.documentType = dt; }

    // ── POLYMORPHISM: abstract method each subclass overrides ──────────────────
    /**
     * Passport   → "Passport #P1234 | Issued by: DFA | Valid until: 2030/01/01"
     * Supporting → "Air Ticket"
     */
    public abstract String getDocumentSummary();

    @Override
    public String toString() {
        return getDocumentSummary();
    }
}
