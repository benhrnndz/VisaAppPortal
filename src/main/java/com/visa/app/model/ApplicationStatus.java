package com.visa.app.model;

/**
 * Enumeration representing the review status of a Visa Application.
 */
public enum ApplicationStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    DENIED("DENIED");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ApplicationStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return PENDING;
        }
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.value.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return PENDING;
    }

    @Override
    public String toString() {
        return value;
    }
}
