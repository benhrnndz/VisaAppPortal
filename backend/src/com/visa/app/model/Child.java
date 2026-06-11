package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Child
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * INHERITANCE  — extends Person, inheriting firstName, lastName,
 *                dateOfBirth without re-declaring them.
 *
 * POLYMORPHISM — Overrides getProfileSummary() with child-specific output.
 *                Called through a Person reference, it behaves differently
 *                from Applicant — same method name, different result.
 *
 * Maps to: `children` table (id, application_id, name, age).
 * Note: The frontend stores one combined "name" string in the DB.
 *       We split it into firstName/lastName for the OOP model here.
 */
public class Child extends Person {

    // ── ENCAPSULATION: private fields ─────────────────────────────────────────
    private int id;
    private int applicationId;
    private int age;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Full constructor — used when loading a row from the `children` table. */
    public Child(int id, int applicationId,
                 String firstName, String lastName, String dateOfBirth, int age) {
        super(firstName, lastName, dateOfBirth);   // INHERITANCE: calls Person(...)
        this.id            = id;
        this.applicationId = applicationId;
        this.age           = age;
    }

    /**
     * Convenience constructor that accepts the combined name string stored by
     * the existing frontend (e.g., "Maria Cruz"). Splits on the first space.
     */
    public Child(int id, int applicationId, String combinedName, int age) {
        this(id, applicationId, splitFirst(combinedName), splitLast(combinedName), "", age);
    }

    /** Brand-new child, not yet persisted. */
    public Child(String combinedName, int age) {
        this(-1, -1, combinedName, age);
    }

    // ── Helpers for splitting combined name ───────────────────────────────────
    private static String splitFirst(String name) {
        if (name == null || !name.contains(" ")) return name == null ? "" : name;
        return name.substring(0, name.indexOf(' '));
    }

    private static String splitLast(String name) {
        if (name == null || !name.contains(" ")) return "";
        return name.substring(name.indexOf(' ') + 1);
    }

    // ── POLYMORPHISM: override getProfileSummary() ────────────────────────────
    @Override
    public String getProfileSummary() {
        return "Dependent Child: " + getFullName()
             + " | Age: " + age
             + " | Application ID: " + applicationId;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int  getId()                       { return id; }
    public void setId(int id)                 { this.id = id; }

    public int  getApplicationId()            { return applicationId; }
    public void setApplicationId(int appId)   { this.applicationId = appId; }

    public int  getAge()                      { return age; }
    public void setAge(int age)               { this.age = age; }

    /** Returns the combined full name — compatible with the existing DB column. */
    public String getCombinedName()           { return getFullName(); }
}
