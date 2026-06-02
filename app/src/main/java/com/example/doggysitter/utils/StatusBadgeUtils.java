package com.example.doggysitter.utils;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.doggysitter.R;

public final class StatusBadgeUtils {
    private StatusBadgeUtils() {
    }

    public static void apply(TextView badgeTextView, String status) {
        int backgroundResource = R.drawable.bg_status_unknown;
        int textColorResource = R.color.ds_status_unknown_text;

        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
            backgroundResource = R.drawable.bg_status_open;
            textColorResource = R.color.ds_status_open_text;
        } else if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            backgroundResource = R.drawable.bg_status_accepted;
            textColorResource = R.color.ds_status_accepted_text;
        } else if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            backgroundResource = R.drawable.bg_status_completed;
            textColorResource = R.color.ds_status_completed_text;
        } else if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            backgroundResource = R.drawable.bg_status_canceled;
            textColorResource = R.color.ds_status_canceled_text;
        }

        badgeTextView.setBackgroundResource(backgroundResource);
        badgeTextView.setTextColor(ContextCompat.getColor(
                badgeTextView.getContext(),
                textColorResource
        ));
    }
}
