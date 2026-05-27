package com.example.doggysitter.utils;


public final class ValidationUtils {
    private static final String HEBREW_NAME_PATTERN = "^[\\u0590-\\u05FF]+(?:[ -][\\u0590-\\u05FF]+)*$";
    private static final String ENGLISH_NAME_PATTERN = "^[A-Za-z]+(?:[ -][A-Za-z]+)*$";

    private ValidationUtils() {
    }

    public static String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public static boolean isValidSingleLanguageName(String value) {
        String normalized = normalizeSpaces(value);
        return normalized.matches(HEBREW_NAME_PATTERN) || normalized.matches(ENGLISH_NAME_PATTERN);
    }

    public static boolean isPositivePrice(String value) {
        try {
            return Double.parseDouble(value) > 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }
}
