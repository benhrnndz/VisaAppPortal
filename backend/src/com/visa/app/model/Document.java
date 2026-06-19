package com.visa.app.model;

/**
 * ============================================================
 *  CLASS: Document
 *  OOP CONCEPT: Encapsulation
 * ============================================================
 *
 * CORRECTED: Per the ERD, DocumentT only ever has
 * (Document_ID, Application_ID, Document_Type). It never carries
 * passport fields — those live in PassportT, a separate entity.
 * Document is therefore no longer an abstract base class with a
 * Passport subtype; SupportingDocument was the only real kind of
 * document, so this class is now concrete on its own.
 *
 * Maps to: `documents` table.
 */
public class Document {

    // ── ENCAPSULATION: private fields, matching DocumentT exactly ─────────────
    private int    id;             // Document_ID
    private int    applicationId;  // Application_ID (FK)
    private String documentType;   // Document_Type

    // ── Constructor ───────────────────────────────────────────────────────────
    public Document(int id, int applicationId, String documentType) {
        this.id            = id;
        this.applicationId = applicationId;
        this.documentType  = documentType;
    }

    public Document(String documentType) {
        this(-1, -1, documentType);
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getId()                       { return id; }
    public void   setId(int id)                 { this.id = id; }

    public int    getApplicationId()            { return applicationId; }
    public void   setApplicationId(int appId)   { this.applicationId = appId; }

    public String getDocumentType()             { return documentType; }
    public void   setDocumentType(String dt)    { this.documentType = dt; }

    public String getDocumentSummary() {
        return "Supporting Document: " + documentType;
    }

    @Override
    public String toString() {
        return getDocumentSummary();
    }
}