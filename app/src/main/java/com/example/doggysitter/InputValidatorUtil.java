package com.example.doggysitter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class InputValidatorUtil {
    // Static method to validate email and password
    public static boolean isValidInput(Context context, String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, context.getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, context.getString(R.string.error_password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
