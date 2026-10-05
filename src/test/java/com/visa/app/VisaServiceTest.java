package com.visa.app;

import com.visa.app.dao.DatabaseConnection;
import com.visa.app.model.Applicant;
import com.visa.app.model.Application;
import com.visa.app.model.ApplicationStatus;
import com.visa.app.model.Child;
import com.visa.app.model.Document;
import com.visa.app.model.Passport;
import com.visa.app.model.Person;
import com.visa.app.model.User;
import com.visa.app.service.VisaService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VisaServiceTest {

    private static VisaService visaService;
    private static final String TEST_DB = "test_visa_app.db";

    @BeforeAll
    public static void setUp() {
        // Use an isolated file database for testing
        new File(TEST_DB).delete();
        DatabaseConnection.setDbUrl("jdbc:sqlite:" + TEST_DB);
        DatabaseConnection.initializeDatabase();
        visaService = VisaService.getInstance();
    }

    @AfterAll
    public static void tearDown() {
        DatabaseConnection.close();
        new File(TEST_DB).delete();
    }

    @Test
    @Order(1)
    public void testDefaultUsersSeeded() {
        User admin = visaService.loginUser("admin@visa.com", "admin123");
        assertNotNull(admin, "Admin should be seeded by default");
        assertTrue(admin.isAdmin());

        User applicant = visaService.loginUser("user@visa.com", "user123");
        assertNotNull(applicant, "Applicant user should be seeded by default");
        assertEquals("APPLICANT", applicant.getRole());
    }

    @Test
    @Order(2)
    public void testUserRegistration() {
        boolean registered = visaService.registerUser("john.doe@example.com", "secret123", "APPLICANT");
        assertTrue(registered, "User registration should succeed");

        User user = visaService.loginUser("john.doe@example.com", "secret123");
        assertNotNull(user);
        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    @Order(3)
    public void testSaveApplicationTransactional() {
        User user = visaService.loginUser("john.doe@example.com", "secret123");
        assertNotNull(user);

        // Build Applicant
        Applicant applicant = Applicant.builder()
                .userId(user.getId())
                .fullName("John Doe")
                .dateOfBirth("1990/05/15")
                .placeOfBirth("Manila")
                .sex("Male")
                .citizenship("Filipino")
                .contactNo("+63 917 123 4567")
                .homeAddress("123 Rizal Ave, Manila")
                .civilStatus("Married")
                .occupation("Software Engineer")
                .employerOfficeAndAddress("Tech Corp BGC")
                .build();

        // Build Application
        Application app = new Application();
        app.setApplicant(applicant);
        app.setPassport(new Passport("P1234567A", "DFA Manila", "2022/01/01", "2032/01/01"));
        app.setRequestedEntryType("Multiple");
        app.setLengthOfStayDays(60);
        app.setPortOfEntry("NAIA");
        app.setPurposeType("Tourism");
        app.setDateOfApplication("2026/01/10");
        app.setAgeUponApplication(35);
        app.setStatus(ApplicationStatus.PENDING);

        // Add dependent child
        app.addChild(new Child("Jimmy Doe", 5));

        // Add supporting documents
        app.addDocument(new Document("Air Ticket"));
        app.addDocument(new Document("Bank Certificate"));
        app.addDocument(new Document("Invitation Letter"));

        boolean saved = visaService.saveApplication(app);
        assertTrue(saved, "Application should save atomically");
        assertTrue(app.getApplicationId() > 0, "Application ID should be generated");
        assertTrue(applicant.getApplicantId() > 0, "Applicant ID should be generated");
    }

    @Test
    @Order(4)
    public void testApplicationStatusUpdate() {
        List<Application> apps = visaService.getAllApplications();
        assertFalse(apps.isEmpty(), "Should have applications saved");

        Application app = apps.get(0);
        int appId = app.getApplicationId();

        // Approve
        boolean approved = visaService.approveApplication(appId);
        assertTrue(approved);

        // Deny
        boolean denied = visaService.denyApplication(appId);
        assertTrue(denied);
    }

    @Test
    @Order(5)
    public void testQueriesQ9Q10Q11() {
        // Q9: 3-table join
        List<String[]> passportRecords = visaService.getApplicantsWithPassportDetails();
        assertNotNull(passportRecords);
        assertFalse(passportRecords.isEmpty(), "Q9 should return passport joined records");

        // Q10: Applications with all 3 supporting docs
        List<String[]> completeApps = visaService.getCompleteApplications();
        assertNotNull(completeApps);
        assertFalse(completeApps.isEmpty(), "Q10 should find application with 3 supporting docs");

        // Q11: Expiring passports
        List<String[]> expiring = visaService.getApplicationsWithExpiringPassports();
        assertNotNull(expiring, "Q11 query should execute without SQL syntax error");
    }

    @Test
    @Order(6)
    public void testPolymorphism() {
        Applicant applicant = Applicant.builder()
                .firstName("Maria")
                .lastName("Clara")
                .citizenship("Filipino")
                .civilStatus("Single")
                .build();

        Child child = new Child("Crispin Clara", 7);

        List<Person> family = List.of(applicant, child);
        for (Person p : family) {
            String summary = p.getProfileSummary();
            assertNotNull(summary);
            assertTrue(summary.contains("Maria Clara") || summary.contains("Crispin Clara"));
        }
        assertDoesNotThrow(() -> visaService.printProfileSummaries(family));
    }
}
