package com.example.doggysitter.utils;

import com.example.doggysitter.R;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public final class ExitConfirmationUtils {
    private ExitConfirmationUtils() {
    }

    public static void install(AppCompatActivity activity) {
        activity.getOnBackPressedDispatcher().addCallback(
                activity,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmation(activity);
                    }
                }
        );
    }

    private static void showExitConfirmation(AppCompatActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.exit_app_title)
                .setMessage(R.string.exit_app_message)
                .setPositiveButton(R.string.exit_app_positive, (dialog, which) -> activity.finishAffinity())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
