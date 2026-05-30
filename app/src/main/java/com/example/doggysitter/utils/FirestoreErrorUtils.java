package com.example.doggysitter.utils;

import com.example.doggysitter.R;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestoreException;

public final class FirestoreErrorUtils {
    private FirestoreErrorUtils() {
    }

    public static int getReadErrorMessageResId(String tag, String operation, Throwable error,
                                               int fallbackMessageResId) {
        log(tag, operation, error);
        return isPermissionDenied(error)
                ? R.string.error_permission_denied_view
                : fallbackMessageResId;
    }

    public static int getWriteErrorMessageResId(String tag, String operation, Throwable error,
                                                int fallbackMessageResId) {
        log(tag, operation, error);
        return isPermissionDenied(error)
                ? R.string.error_permission_denied_action
                : fallbackMessageResId;
    }

    public static int getSecuritySensitiveWriteErrorMessageResId(String tag, String operation,
                                                                 Throwable error,
                                                                 int fallbackMessageResId) {
        log(tag, operation, error);
        return isPermissionDenied(error)
                ? R.string.error_action_blocked_security
                : fallbackMessageResId;
    }

    public static void log(String tag, String operation, Throwable error) {
        Log.e(tag, operation, error);
    }

    public static boolean isPermissionDenied(Throwable error) {
        while (error != null) {
            if (error instanceof FirebaseFirestoreException
                    && ((FirebaseFirestoreException) error).getCode()
                    == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return true;
            }
            error = error.getCause();
        }
        return false;
    }
}
