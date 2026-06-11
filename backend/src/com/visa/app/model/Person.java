package com.visa.app.model;

/**
 * ============================================================
 *  ABSTRACT BASE CLASS: Person
 *  OOP CONCEPT: Abstraction + Encapsulation + Polymorphism
 * ============================================================
 *
 * ABSTRACTION  — This class is declared abstract. You can never write
 *                `new Person(...)` directly. It exists only to be extended.
 *
 * ENCAPSULATION — firstName, lastName, dateOfBirth are private. External
 *                 code must use getters/setters — it cannot touch the raw
 *                 fields directly.
 *
 * POLYMORPHISM  — getProfileSummary() is declared abstract here. Every
 *                 subclass (Applicant, Child) must override it and return
 *                 its own formatted string. When you call the method through
 *                 a Person reference, Java dispatches to the correct subclass
 *                 version at runtime — this is runtime polymorphism.
 *
 *                 Person p = new Applicant(...);
 *                 p.getProfileSummary(); // → "Primary Applicant: Juan dela Cruz"
 *
 *                 Person c = new Child(...);
 *                 c.getProfileSummary(); // → "Dependent Child: Maria dela Cruz, Age 8"
 *                 Same call, different output.
 */
public abstract class Person {

    // ── ENCAPSULATION: all fields are private ─────────────────────────────────
    private String firstName;
    private String lastName;
    private String dateOfBirth;   // format: YYYY/MM/DD

    // ── Constructor ───────────────────────────────────────────────────────────
    public Person(String firstName, String lastName, String dateOfBirth) {
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    // ── Getters & Setters (Encapsulation: controlled access) ──────────────────
    public String getFirstName()             { return firstName; }
    public void   setFirstName(String fn)    { this.firstName = fn; }

    public String getLastName()              { return lastName; }
    public void   setLastName(String ln)     { this.lastName = ln; }

    public String getDateOfBirth()           { return dateOfBirth; }
    public void   setDateOfBirth(String dob) { this.dateOfBirth = dob; }

    /** Convenience helper: combines first + last name. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── POLYMORPHISM: abstract method every subclass must implement ────────────
    /**
     * Returns a role-specific profile summary string.
     * Applicant overrides this → "Primary Applicant: …"
     * Child     overrides this → "Dependent Child: …"
     */
    public abstract String getProfileSummary();

    @Override
    public String toString() {
        return getProfileSummary();
    }
}
