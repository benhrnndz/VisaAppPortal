package com.visa.app.util;

/**
 * Common utilities for personal name parsing.
 */
public final class NameUtils {

    private NameUtils() {}

    public static String splitFirst(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        String trimmed = fullName.trim();
        int idx = trimmed.indexOf(' ');
        return idx < 0 ? trimmed : trimmed.substring(0, idx);
    }

    public static String splitLast(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        String trimmed = fullName.trim();
        int idx = trimmed.indexOf(' ');
        return idx < 0 ? "" : trimmed.substring(idx + 1).trim();
    }
}
