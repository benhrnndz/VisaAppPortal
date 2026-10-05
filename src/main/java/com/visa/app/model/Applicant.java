package com.visa.app.model;

/**
 * ============================================================
 *  MODEL: Applicant
 *  OOP CONCEPT: Inheritance + Polymorphism + Encapsulation
 * ============================================================
 *
 * Biographic and demographic profile of an applicant.
 * Maps to: `applicants` table (FK user_id -> `users`).
 */
public class Applicant extends Person {

    private int applicantId;
    private int userId;
    private String placeOfBirth;
    private String sex;
    private String citizenship;
    private String contactNo;
    private String homeAddress;
    private String civilStatus;
    private String spouseName;
    private String occupation;
    private String employerOfficeAndAddress;
    private String fatherName;
    private String motherName;

    // Transient field carried from users table during join queries
    private String transientEmail = "";

    public Applicant() {
        super("", "", "");
        this.applicantId = -1;
        this.userId = -1;
        this.placeOfBirth = "";
        this.sex = "Male";
        this.citizenship = "";
        this.contactNo = "";
        this.homeAddress = "";
        this.civilStatus = "Single";
        this.spouseName = "";
        this.occupation = "";
        this.employerOfficeAndAddress = "";
        this.fatherName = "";
        this.motherName = "";
    }

    public Applicant(int applicantId, int userId,
                     String firstName, String lastName, String dateOfBirth,
                     String placeOfBirth, String sex, String citizenship,
                     String contactNo, String homeAddress, String civilStatus,
                     String spouseName, String occupation, String employerOfficeAndAddress,
                     String fatherName, String motherName) {
        super(firstName, lastName, dateOfBirth);
        this.applicantId = applicantId;
        this.userId = userId;
        this.placeOfBirth = placeOfBirth != null ? placeOfBirth.trim() : "";
        this.sex = sex != null ? sex.trim() : "Male";
        this.citizenship = citizenship != null ? citizenship.trim() : "";
        this.contactNo = contactNo != null ? contactNo.trim() : "";
        this.homeAddress = homeAddress != null ? homeAddress.trim() : "";
        this.civilStatus = civilStatus != null ? civilStatus.trim() : "Single";
        this.spouseName = spouseName != null ? spouseName.trim() : "";
        this.occupation = occupation != null ? occupation.trim() : "";
        this.employerOfficeAndAddress = employerOfficeAndAddress != null ? employerOfficeAndAddress.trim() : "";
        this.fatherName = fatherName != null ? fatherName.trim() : "";
        this.motherName = motherName != null ? motherName.trim() : "";
    }

    public Applicant(String firstName, String lastName, String dateOfBirth,
                     String placeOfBirth, String sex, String citizenship,
                     String contactNo, String homeAddress, String civilStatus) {
        this(-1, -1, firstName, lastName, dateOfBirth, placeOfBirth, sex, citizenship,
             contactNo, homeAddress, civilStatus, "", "", "", "", "");
    }

    @Override
    public String getProfileSummary() {
        return "Primary Applicant: " + getFullName()
                + " | Citizenship: " + citizenship
                + " | Civil Status: " + civilStatus;
    }

    // ── Builder Pattern for clean, readable instantiation ────────────────────
    public static class Builder {
        private int applicantId = -1;
        private int userId = -1;
        private String firstName = "";
        private String lastName = "";
        private String dateOfBirth = "";
        private String placeOfBirth = "";
        private String sex = "Male";
        private String citizenship = "";
        private String contactNo = "";
        private String homeAddress = "";
        private String civilStatus = "Single";
        private String spouseName = "";
        private String occupation = "";
        private String employerOfficeAndAddress = "";
        private String fatherName = "";
        private String motherName = "";
        private String transientEmail = "";

        public Builder applicantId(int val) { this.applicantId = val; return this; }
        public Builder userId(int val) { this.userId = val; return this; }
        public Builder firstName(String val) { this.firstName = val; return this; }
        public Builder lastName(String val) { this.lastName = val; return this; }
        public Builder dateOfBirth(String val) { this.dateOfBirth = val; return this; }
        public Builder placeOfBirth(String val) { this.placeOfBirth = val; return this; }
        public Builder sex(String val) { this.sex = val; return this; }
        public Builder citizenship(String val) { this.citizenship = val; return this; }
        public Builder contactNo(String val) { this.contactNo = val; return this; }
        public Builder homeAddress(String val) { this.homeAddress = val; return this; }
        public Builder civilStatus(String val) { this.civilStatus = val; return this; }
        public Builder spouseName(String val) { this.spouseName = val; return this; }
        public Builder occupation(String val) { this.occupation = val; return this; }
        public Builder employerOfficeAndAddress(String val) { this.employerOfficeAndAddress = val; return this; }
        public Builder fatherName(String val) { this.fatherName = val; return this; }
        public Builder motherName(String val) { this.motherName = val; return this; }
        public Builder transientEmail(String val) { this.transientEmail = val; return this; }

        public Builder fullName(String fullName) {
            if (fullName != null && !fullName.isBlank()) {
                String trimmed = fullName.trim();
                int idx = trimmed.indexOf(' ');
                if (idx < 0) {
                    this.firstName = trimmed;
                    this.lastName = "";
                } else {
                    this.firstName = trimmed.substring(0, idx);
                    this.lastName = trimmed.substring(idx + 1).trim();
                }
            }
            return this;
        }

        public Applicant build() {
            Applicant a = new Applicant(applicantId, userId, firstName, lastName, dateOfBirth,
                    placeOfBirth, sex, citizenship, contactNo, homeAddress, civilStatus,
                    spouseName, occupation, employerOfficeAndAddress, fatherName, motherName);
            a.setTransientEmail(transientEmail);
            return a;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Getters and Setters ───────────────────────────────────────────────────
    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth != null ? placeOfBirth : ""; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex != null ? sex : ""; }

    public String getCitizenship() { return citizenship; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship != null ? citizenship : ""; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo != null ? contactNo : ""; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress != null ? homeAddress : ""; }

    public String getCivilStatus() { return civilStatus; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus != null ? civilStatus : ""; }

    public String getSpouseName() { return spouseName; }
    public void setSpouseName(String spouseName) { this.spouseName = spouseName != null ? spouseName : ""; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation != null ? occupation : ""; }

    public String getEmployerOfficeAndAddress() { return employerOfficeAndAddress; }
    public void setEmployerOfficeAndAddress(String val) { this.employerOfficeAndAddress = val != null ? val : ""; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName != null ? fatherName : ""; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName != null ? motherName : ""; }

    public String getTransientEmail() { return transientEmail; }
    public void setTransientEmail(String email) { this.transientEmail = email != null ? email : ""; }
}
