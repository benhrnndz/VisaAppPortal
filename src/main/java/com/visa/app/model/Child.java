package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Child
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * Dependent child of an applicant.
 * Foreign key is applicant_id (Child belongs to Applicant).
 * Maps to: `children` table (child_id, applicant_id, child_name, child_age).
 */
public class Child extends Person {

    private int childId;
    private int applicantId;
    private int age;

    public Child(int childId, int applicantId, String childName, int age) {
        super(splitFirst(childName), splitLast(childName), "");
        this.childId = childId;
        this.applicantId = applicantId;
        this.age = Math.max(0, age);
    }

    public Child(String childName, int age) {
        this(-1, -1, childName, age);
    }

    public Child(int id, int applicationId, String name, int age, boolean legacyAppId) {
        // Compatibility constructor for legacy code
        this(id, -1, name, age);
    }

    private static String splitFirst(String name) {
        if (name == null || name.isBlank()) return "";
        String trimmed = name.trim();
        int idx = trimmed.indexOf(' ');
        return idx < 0 ? trimmed : trimmed.substring(0, idx);
    }

    private static String splitLast(String name) {
        if (name == null || name.isBlank()) return "";
        String trimmed = name.trim();
        int idx = trimmed.indexOf(' ');
        return idx < 0 ? "" : trimmed.substring(idx + 1).trim();
    }

    @Override
    public String getProfileSummary() {
        return "Dependent Child: " + getName() + " (Age: " + age + ")";
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    // Compatibility getter/setter for legacy code that called getId()
    public int getId() {
        return childId;
    }

    public void setId(int id) {
        this.childId = id;
    }

    public int getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(int applicantId) {
        this.applicantId = applicantId;
    }

    // Compatibility getter/setter for legacy code that used applicationId
    public int getApplicationId() {
        return applicantId;
    }

    public void setApplicationId(int applicationId) {
        this.applicantId = applicationId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public String getName() {
        return getFullName();
    }

    public void setName(String name) {
        setFirstName(splitFirst(name));
        setLastName(splitLast(name));
    }
}
