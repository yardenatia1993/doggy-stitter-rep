package com.example.doggysitter.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public final class AdminDisplayUtils {
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private AdminDisplayUtils() {
    }

    public static String fallbackText(String value) {
        String normalized = ValidationUtils.normalizeSpaces(value);
        return normalized.isEmpty() ? "לא צוין" : normalized;
    }

    public static String displayUserName(String userId, Map<String, String> userNamesById) {
        String normalizedId = ValidationUtils.normalizeSpaces(userId);
        if (normalizedId.isEmpty()) {
            return "לא ידוע";
        }

        String fullName = userNamesById == null ? "" : ValidationUtils.normalizeSpaces(
                userNamesById.get(normalizedId)
        );
        return fullName.isEmpty() ? normalizedId : fullName;
    }

    public static String formatCreatedAt(Object value) {
        if (value instanceof Timestamp) {
            return DATE_TIME_FORMAT.format(((Timestamp) value).toDate());
        }
        if (value instanceof Date) {
            return DATE_TIME_FORMAT.format((Date) value);
        }
        if (value instanceof String) {
            return fallbackText((String) value);
        }
        return "לא צוין";
    }

    public static String formatRole(String role) {
        if (FirestoreConstants.ROLE_OWNER.equals(role)) {
            return "בעל כלב";
        }
        if (FirestoreConstants.ROLE_WALKER.equals(role)) {
            return "דוגווקר";
        }
        if (FirestoreConstants.ROLE_ADMIN.equals(role)) {
            return "מנהל";
        }
        return "לא ידוע";
    }

    public static String formatWalkRequestStatus(String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
            return "פתוחה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            return "התקבלה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            return "הושלמה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            return "בוטלה";
        }
        return "לא ידוע";
    }

    public static String formatActive(boolean active) {
        return active ? "פעיל" : "לא פעיל";
    }

    public static String formatReviewed(Boolean reviewed) {
        return Boolean.TRUE.equals(reviewed) ? "כן" : "לא";
    }

    public static String formatRating(Double averageRating) {
        if (averageRating == null) {
            return "לא צוין";
        }
        return String.format(Locale.getDefault(), "%.1f", averageRating);
    }

    public static String formatStars(int rating) {
        int fullStars = Math.max(0, Math.min(5, rating));
        StringBuilder stars = new StringBuilder();
        for (int index = 0; index < 5; index++) {
            stars.append(index < fullStars ? '★' : '☆');
        }
        return stars.toString();
    }
}
