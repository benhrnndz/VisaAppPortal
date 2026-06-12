package com.visa.app;

import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. USER MODEL
// ==========================================
class User {
    private int id;
    private String email;
    private String password;
    private String role; // "APPLICANT" or "ADMIN"

    public User(int id, String email, String password, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String email, String password, String role) {
        this(-1, email, password, role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}

// ==========================================
// 2. CHILD MODEL
// ==========================================
class Child {
    private int id;
    private int applicationId;
    private String name;
    private int age;

    public Child(int id, int applicationId, String name, int age) {
        this.id = id;
        this.applicationId = applicationId;
        this.name = name;
        this.age = age;
    }

    public Child(String name, int age) {
        this(-1, -1, name, age);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

// ==========================================
// 3. DOCUMENT MODEL
// ==========================================
class Document {
    private int id;
    private int applicationId;
    private String documentType; // "Original Passport", "Air Ticket", "Invitation Letter", "Bank Certificate"
    
    // Passport specific fields (null if not a passport)
    private String passportNumber;
    private String issuingAuthority;
    private String dateIssued;   // YYYY/MM/DD
    private String validityDate; // YYYY/MM/DD

    public Document(int id, int applicationId, String documentType, String passportNumber, String issuingAuthority, String dateIssued, String validityDate) {
        this.id = id;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.passportNumber = passportNumber;
        this.issuingAuthority = issuingAuthority;
        this.dateIssued = dateIssued;
        this.validityDate = validityDate;
    }

    public Document(String documentType, String passportNumber, String issuingAuthority, String dateIssued, String validityDate) {
        this(-1, -1, documentType, passportNumber, issuingAuthority, dateIssued, validityDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getDocumentType() {
        return documentType == null ? "" : documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getPassportNumber() {
        return passportNumber == null ? "" : passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getIssuingAuthority() {
        return issuingAuthority == null ? "" : issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getDateIssued() {
        return dateIssued == null ? "" : dateIssued;
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getValidityDate() {
        return validityDate == null ? "" : validityDate;
    }

    public void setValidityDate(String validityDate) {
        this.validityDate = validityDate;
    }
}

// ==========================================
// 4. VISA APPLICATION MODEL
// ==========================================
class VisaApplication {
    private int id;
    private int userId;
    
    // Personal Information (Required)
    private String fullName;
    private String sex;
    private String citizenship;
    private String civilStatus;
    private String birthDate;
    private String placeOfBirth;
    private String email;
    private String contactNumber;
    private String homeAddress;

    // Family Information (Optional)
    private String fatherName;
    private String motherName;
    private String spouseName;
    private boolean withChildren;
    private List<Child> children;

    // Employment Information (Optional)
    private String occupation;
    private String employerAddress;

    // Travel Information (Required documents)
    private List<Document> documents;

    // Application Status
    private String status; // "PENDING", "APPROVED", "DENIED"

    // Application Details (Required/Optional)
    private String entryType;
    private int lengthOfStay;
    private String portOfEntry;
    private String destinationAfter;
    private int ageUponApp;
    private String dateOfApp;
    private String purposeType;
    private String sponsorName;
    private String sponsorContact;

    public VisaApplication(int id, int userId, String fullName, String sex, String citizenship, String civilStatus,
                           String birthDate, String placeOfBirth, String email, String contactNumber, String homeAddress,
                           String fatherName, String motherName, String spouseName, boolean withChildren,
                           String occupation, String employerAddress, String status) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.sex = sex;
        this.citizenship = citizenship;
        this.civilStatus = civilStatus;
        this.birthDate = birthDate;
        this.placeOfBirth = placeOfBirth;
        this.email = email;
        this.contactNumber = contactNumber;
        this.homeAddress = homeAddress;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.spouseName = spouseName;
        this.withChildren = withChildren;
        this.occupation = occupation;
        this.employerAddress = employerAddress;
        this.status = status;
        this.children = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.entryType = "";
        this.lengthOfStay = 0;
        this.portOfEntry = "";
        this.destinationAfter = "";
        this.ageUponApp = 0;
        this.dateOfApp = "";
        this.purposeType = "";
        this.sponsorName = "";
        this.sponsorContact = "";
    }

    public VisaApplication() {
        this(-1, -1, "", "Male", "", "Single", "", "", "", "", "", "", "", "", false, "", "", "PENDING");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName == null ? "" : fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSex() {
        return sex == null ? "" : sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCitizenship() {
        return citizenship == null ? "" : citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getCivilStatus() {
        return civilStatus == null ? "" : civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public String getBirthDate() {
        return birthDate == null ? "" : birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth == null ? "" : placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber == null ? "" : contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getHomeAddress() {
        return homeAddress == null ? "" : homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getFatherName() {
        return fatherName == null ? "" : fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName == null ? "" : motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getSpouseName() {
        return spouseName == null ? "" : spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public boolean isWithChildren() {
        return withChildren;
    }

    public void setWithChildren(boolean withChildren) {
        this.withChildren = withChildren;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public void addChild(Child child) {
        this.children.add(child);
    }

    public String getOccupation() {
        return occupation == null ? "" : occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEmployerAddress() {
        return employerAddress == null ? "" : employerAddress;
    }

    public void setEmployerAddress(String employerAddress) {
        this.employerAddress = employerAddress;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public void addDocument(Document document) {
        this.documents.add(document);
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters and Setters for Application Details
    public String getEntryType() {
        return entryType == null ? "" : entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public int getLengthOfStay() {
        return lengthOfStay;
    }

    public void setLengthOfStay(int lengthOfStay) {
        this.lengthOfStay = lengthOfStay;
    }

    public String getPortOfEntry() {
        return portOfEntry == null ? "" : portOfEntry;
    }

    public void setPortOfEntry(String portOfEntry) {
        this.portOfEntry = portOfEntry;
    }

    public String getDestinationAfter() {
        return destinationAfter == null ? "" : destinationAfter;
    }

    public void setDestinationAfter(String destinationAfter) {
        this.destinationAfter = destinationAfter;
    }

    public int getAgeUponApp() {
        return ageUponApp;
    }

    public void setAgeUponApp(int ageUponApp) {
        this.ageUponApp = ageUponApp;
    }

    public String getDateOfApp() {
        return dateOfApp == null ? "" : dateOfApp;
    }

    public void setDateOfApp(String dateOfApp) {
        this.dateOfApp = dateOfApp;
    }

    public String getPurposeType() {
        return purposeType == null ? "" : purposeType;
    }

    public void setPurposeType(String purposeType) {
        this.purposeType = purposeType;
    }

    public String getSponsorName() {
        return sponsorName == null ? "" : sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getSponsorContact() {
        return sponsorContact == null ? "" : sponsorContact;
    }

    public void setSponsorContact(String sponsorContact) {
        this.sponsorContact = sponsorContact;
    }
}
