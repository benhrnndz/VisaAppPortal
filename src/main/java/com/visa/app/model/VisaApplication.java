package com.visa.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  VIEW MODEL / FORM DTO: VisaApplication
 * ============================================================
 *
 * Composite model used by the UI wizard and dashboards to present
 * and edit an entire visa application package.
 * Seamlessly maps to/from the normalized 3NF domain entities:
 *   - Applicant (personal/biographical details)
 *   - Application (travel details)
 *   - Passport (national passport record)
 *   - List<Child> (dependents)
 *   - List<Document> (supporting documents)
 */
public class VisaApplication {
    private int id;
    private int userId;

    // Personal Information
    private String fullName;
    private String sex;
    private String citizenship;
    private String civilStatus;
    private String birthDate;
    private String placeOfBirth;
    private String email;
    private String contactNumber;
    private String homeAddress;

    // Family Information
    private String fatherName;
    private String motherName;
    private String spouseName;
    private boolean withChildren;
    private List<Child> children;

    // Employment Information
    private String occupation;
    private String employerAddress;

    // Travel Documents
    private List<Document> documents;

    // Application Status
    private String status;

    // Application Details
    private String entryType;
    private int lengthOfStay;
    private String portOfEntry;
    private String destinationAfter;
    private int ageUponApp;
    private String dateOfApp;
    private String purposeType;
    private String sponsorName;
    private String sponsorContact;

    public VisaApplication() {
        this(-1, -1, "", "Male", "", "Single", "", "", "", "", "", "", "", "", false, "", "", "PENDING");
    }

    public VisaApplication(int id, int userId, String fullName, String sex, String citizenship, String civilStatus,
                           String birthDate, String placeOfBirth, String email, String contactNumber, String homeAddress,
                           String fatherName, String motherName, String spouseName, boolean withChildren,
                           String occupation, String employerAddress, String status) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName != null ? fullName : "";
        this.sex = sex != null ? sex : "Male";
        this.citizenship = citizenship != null ? citizenship : "";
        this.civilStatus = civilStatus != null ? civilStatus : "Single";
        this.birthDate = birthDate != null ? birthDate : "";
        this.placeOfBirth = placeOfBirth != null ? placeOfBirth : "";
        this.email = email != null ? email : "";
        this.contactNumber = contactNumber != null ? contactNumber : "";
        this.homeAddress = homeAddress != null ? homeAddress : "";
        this.fatherName = fatherName != null ? fatherName : "";
        this.motherName = motherName != null ? motherName : "";
        this.spouseName = spouseName != null ? spouseName : "";
        this.withChildren = withChildren;
        this.occupation = occupation != null ? occupation : "";
        this.employerAddress = employerAddress != null ? employerAddress : "";
        this.status = status != null ? status : "PENDING";
        this.children = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.entryType = "Single";
        this.lengthOfStay = 30;
        this.portOfEntry = "NAIA";
        this.destinationAfter = "";
        this.ageUponApp = 0;
        this.dateOfApp = "";
        this.purposeType = "Tourism";
        this.sponsorName = "";
        this.sponsorContact = "";
    }

    /**
     * Converts this composite view model into a normalized domain Application entity.
     */
    public Application toDomainApplication() {
        Applicant applicant = Applicant.builder()
                .userId(userId)
                .fullName(fullName)
                .sex(sex)
                .citizenship(citizenship)
                .civilStatus(civilStatus)
                .dateOfBirth(birthDate)
                .placeOfBirth(placeOfBirth)
                .contactNo(contactNumber)
                .homeAddress(homeAddress)
                .fatherName(fatherName)
                .motherName(motherName)
                .spouseName(spouseName)
                .occupation(occupation)
                .employerOfficeAndAddress(employerAddress)
                .transientEmail(email)
                .build();

        // Extract passport details from documents list
        Passport passport = null;
        String passportNo = "N/A-" + userId;
        if (documents != null) {
            for (Document doc : documents) {
                if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())
                        && doc.getPassportNumber() != null && !doc.getPassportNumber().isBlank()) {
                    passportNo = doc.getPassportNumber();
                    passport = new Passport(
                            passportNo,
                            doc.getIssuingAuthority().isBlank() ? "Unknown" : doc.getIssuingAuthority(),
                            doc.getDateIssued().isBlank() ? "2020/01/01" : doc.getDateIssued(),
                            doc.getValidityDate().isBlank() ? "2030/01/01" : doc.getValidityDate()
                    );
                    break;
                }
            }
        }
        if (passport == null) {
            passport = new Passport(passportNo, "Pending", "2020/01/01", "2030/01/01");
        }

        Application app = new Application(
                id,
                applicant.getApplicantId(),
                passportNo,
                entryType,
                lengthOfStay,
                portOfEntry,
                destinationAfter,
                ageUponApp,
                dateOfApp,
                purposeType,
                sponsorName,
                sponsorContact,
                status
        );

        app.setApplicant(applicant);
        app.setPassport(passport);

        if (children != null) {
            for (Child c : children) {
                app.addChild(c);
            }
        }

        if (documents != null) {
            for (Document d : documents) {
                app.addDocument(d);
            }
        }

        return app;
    }

    /**
     * Converts a normalized domain Application entity into this composite view model.
     */
    public static VisaApplication fromDomainApplication(Application app) {
        if (app == null) return null;

        Applicant ap = app.getApplicant();
        VisaApplication va = new VisaApplication();
        va.setId(app.getApplicationId());
        va.setStatus(app.getStatus() != null ? app.getStatus().getValue() : "PENDING");
        va.setEntryType(app.getRequestedEntryType());
        va.setLengthOfStay(app.getLengthOfStayDays());
        va.setPortOfEntry(app.getPortOfEntry());
        va.setDestinationAfter(app.getDestAfterPH());
        va.setAgeUponApp(app.getAgeUponApplication());
        va.setDateOfApp(app.getDateOfApplication());
        va.setPurposeType(app.getPurposeType());
        va.setSponsorName(app.getSponsorName());
        va.setSponsorContact(app.getSponContactNo());

        if (ap != null) {
            va.setUserId(ap.getUserId());
            va.setFullName(ap.getFullName());
            va.setSex(ap.getSex());
            va.setCitizenship(ap.getCitizenship());
            va.setCivilStatus(ap.getCivilStatus());
            va.setBirthDate(ap.getDateOfBirth());
            va.setPlaceOfBirth(ap.getPlaceOfBirth());
            va.setEmail(ap.getTransientEmail());
            va.setContactNumber(ap.getContactNo());
            va.setHomeAddress(ap.getHomeAddress());
            va.setFatherName(ap.getFatherName());
            va.setMotherName(ap.getMotherName());
            va.setSpouseName(ap.getSpouseName());
            va.setOccupation(ap.getOccupation());
            va.setEmployerAddress(ap.getEmployerOfficeAndAddress());
        }

        // Add children
        if (app.getChildren() != null && !app.getChildren().isEmpty()) {
            va.setWithChildren(true);
            for (Child c : app.getChildren()) {
                va.addChild(c);
            }
        }

        // Add passport as Document for UI display
        if (app.getPassport() != null && !app.getPassport().getPassportNo().startsWith("N/A-")) {
            Passport p = app.getPassport();
            va.addDocument(new Document(
                    -1, app.getApplicationId(),
                    "Original Passport",
                    p.getPassportNo(),
                    p.getIssuedBy(),
                    p.getDateOfIssue(),
                    p.getValidUntil()
            ));
        }

        // Add supporting documents
        if (app.getDocuments() != null) {
            for (Document d : app.getDocuments()) {
                if (!"Original Passport".equalsIgnoreCase(d.getDocumentType())) {
                    va.addDocument(d);
                }
            }
        }

        return va;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName != null ? fullName : ""; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSex() { return sex != null ? sex : ""; }
    public void setSex(String sex) { this.sex = sex; }

    public String getCitizenship() { return citizenship != null ? citizenship : ""; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship; }

    public String getCivilStatus() { return civilStatus != null ? civilStatus : ""; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }

    public String getBirthDate() { return birthDate != null ? birthDate : ""; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getPlaceOfBirth() { return placeOfBirth != null ? placeOfBirth : ""; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber != null ? contactNumber : ""; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getHomeAddress() { return homeAddress != null ? homeAddress : ""; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getFatherName() { return fatherName != null ? fatherName : ""; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName != null ? motherName : ""; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getSpouseName() { return spouseName != null ? spouseName : ""; }
    public void setSpouseName(String spouseName) { this.spouseName = spouseName; }

    public boolean isWithChildren() { return withChildren; }
    public void setWithChildren(boolean withChildren) { this.withChildren = withChildren; }

    public List<Child> getChildren() { return children; }
    public void setChildren(List<Child> children) { this.children = children != null ? children : new ArrayList<>(); }
    public void addChild(Child child) { if (child != null) this.children.add(child); }

    public String getOccupation() { return occupation != null ? occupation : ""; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getEmployerAddress() { return employerAddress != null ? employerAddress : ""; }
    public void setEmployerAddress(String employerAddress) { this.employerAddress = employerAddress; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents != null ? documents : new ArrayList<>(); }
    public void addDocument(Document document) { if (document != null) this.documents.add(document); }

    public String getStatus() { return status != null ? status : ""; }
    public void setStatus(String status) { this.status = status; }

    public String getEntryType() { return entryType != null ? entryType : ""; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public int getLengthOfStay() { return lengthOfStay; }
    public void setLengthOfStay(int lengthOfStay) { this.lengthOfStay = lengthOfStay; }

    public String getPortOfEntry() { return portOfEntry != null ? portOfEntry : ""; }
    public void setPortOfEntry(String portOfEntry) { this.portOfEntry = portOfEntry; }

    public String getDestinationAfter() { return destinationAfter != null ? destinationAfter : ""; }
    public void setDestinationAfter(String destinationAfter) { this.destinationAfter = destinationAfter; }

    public int getAgeUponApp() { return ageUponApp; }
    public void setAgeUponApp(int ageUponApp) { this.ageUponApp = ageUponApp; }

    public String getDateOfApp() { return dateOfApp != null ? dateOfApp : ""; }
    public void setDateOfApp(String dateOfApp) { this.dateOfApp = dateOfApp; }

    public String getPurposeType() { return purposeType != null ? purposeType : ""; }
    public void setPurposeType(String purposeType) { this.purposeType = purposeType; }

    public String getSponsorName() { return sponsorName != null ? sponsorName : ""; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }

    public String getSponsorContact() { return sponsorContact != null ? sponsorContact : ""; }
    public void setSponsorContact(String sponsorContact) { this.sponsorContact = sponsorContact; }
}
