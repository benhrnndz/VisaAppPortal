package com.visa.app.model;

/**
 * Data Transfer Object for table views in the user and admin dashboards.
 * Avoids leaky abstractions of raw String[] arrays across layers.
 */
public class ApplicationSummaryDTO {
    private final int applicationId;
    private final String applicantName;
    private final String citizenship;
    private final String status;
    private final int documentCount;

    public ApplicationSummaryDTO(int applicationId, String applicantName, String citizenship,
                                 String status, int documentCount) {
        this.applicationId = applicationId;
        this.applicantName = applicantName != null ? applicantName : "";
        this.citizenship = citizenship != null ? citizenship : "";
        this.status = status != null ? status : "PENDING";
        this.documentCount = documentCount;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public String getStatus() {
        return status;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    /**
     * Converts to string array for Swing DefaultTableModel row compatibility.
     */
    public String[] toStringArray() {
        return new String[]{
                String.valueOf(applicationId),
                applicantName,
                citizenship,
                status,
                String.valueOf(documentCount)
        };
    }
}
