package com.visa.app.model;

/**
 * Enumeration representing valid travel document types.
 */
public enum DocumentType {
    ORIGINAL_PASSPORT("Original Passport"),
    AIR_TICKET("Air Ticket"),
    INVITATION_LETTER("Invitation Letter"),
    BANK_CERTIFICATE("Bank Certificate");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DocumentType fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (DocumentType dt : DocumentType.values()) {
            if (dt.displayName.equalsIgnoreCase(text.trim()) || dt.name().equalsIgnoreCase(text.trim())) {
                return dt;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
