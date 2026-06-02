package com.example.doggysitter.utils;

import com.example.doggysitter.R;

import android.content.Context;
import android.text.TextUtils;
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

    public static String getTransitionWriteErrorMessage(Context context, String tag, String operation,
                                                        Throwable error, int fallbackMessageResId) {
        log(tag, operation, error);
        if (isPermissionDenied(error)) {
            return context.getString(R.string.error_permission_denied_action);
        }

        FirebaseFirestoreException firestoreError = findFirestoreError(error);
        if (firestoreError != null
                && firestoreError.getCode() == FirebaseFirestoreException.Code.ABORTED
                && !TextUtils.isEmpty(firestoreError.getMessage())) {
            return firestoreError.getMessage();
        }
        return context.getString(fallbackMessageResId);
    }

    public static void log(String tag, String operation, Throwable error) {
        Log.e(tag, operation, error);
    }

    public static boolean isPermissionDenied(Throwable error) {
        FirebaseFirestoreException firestoreError = findFirestoreError(error);
        return firestoreError != null
                && firestoreError.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED;
    }

    private static FirebaseFirestoreException findFirestoreError(Throwable error) {
        while (error != null) {
            if (error instanceof FirebaseFirestoreException) {
                return (FirebaseFirestoreException) error;
            }
            error = error.getCause();
        }
        return null;
    }
}
