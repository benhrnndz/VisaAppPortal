package com.visa.app.model;

/**
 * ============================================================
 *  ABSTRACT BASE CLASS: Person
 *  OOP CONCEPT: Abstraction + Encapsulation + Polymorphism
 * ============================================================
 *
 * ABSTRACTION   — Abstract base class providing common biographic attributes.
 * ENCAPSULATION — Fields are private with controlled accessors and mutators.
 * POLYMORPHISM  — Defines abstract getProfileSummary() overridden by subclasses.
 */
public abstract class Person {

    private String firstName;
    private String lastName;
    private String dateOfBirth; // format: YYYY/MM/DD

    public Person(String firstName, String lastName, String dateOfBirth) {
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.dateOfBirth = dateOfBirth != null ? dateOfBirth.trim() : "";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : "";
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : "";
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth != null ? dateOfBirth.trim() : "";
    }

    /**
     * Combines first and last names.
     */
    public String getFullName() {
        if (firstName.isEmpty()) return lastName;
        if (lastName.isEmpty()) return firstName;
        return firstName + " " + lastName;
    }

    /**
     * Polymorphic method overridden by subclasses (Applicant, Child).
     */
    public abstract String getProfileSummary();

    @Override
    public String toString() {
        return getProfileSummary();
    }
}
