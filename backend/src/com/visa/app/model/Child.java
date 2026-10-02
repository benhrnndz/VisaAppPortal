package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Child
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * CORRECTED: Per the ERD, ChildT's foreign key is Applicant_ID
 * (a child "belongs_to" an Applicant), NOT Application_ID. The old
 * version of this class wrongly attached children to applications.
 *
 * Maps to: `children` table (child_id, applicant_id, child_name, child_age).
 */
public class Child extends Person {

    // ── ENCAPSULATION: private fields ─────────────────────────────────────────
    private int childId;
    private int applicantId;   // FK -> applicants(applicant_id)
    private int age;

    /** Full constructor — used when loading a row from the `children` table. */
    public Child(int childId, int applicantId, String childName, int age) {
        super(splitFirst(childName), splitLast(childName), "");   // INHERITANCE
        this.childId     = childId;
        this.applicantId = applicantId;
        this.age          = age;
    }

    /** Brand-new child, not yet persisted. */
    public Child(String childName, int age) {
        this(-1, -1, childName, age);
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
        return "Child: " + getFullName()
             + " | Age: " + age
             + " | Applicant ID: " + applicantId;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int  getChildId()                  { return childId; }
    public void setChildId(int id)            { this.childId = id; }

    public int  getApplicantId()              { return applicantId; }
    public void setApplicantId(int id)        { this.applicantId = id; }

    public int  getAge()                      { return age; }
    public void setAge(int age)               { this.age = age; }

    public String getCombinedName()           { return getFullName(); }
}