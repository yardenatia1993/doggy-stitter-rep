package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.repositories.UserRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.FirestoreErrorUtils;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class AdminBaseActivity extends AppCompatActivity {
    protected void requireAdminAccess(Runnable onAccessGranted) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            denyAdminAccess();
            return;
        }

        new UserRepository().getUser(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString(FirestoreConstants.FIELD_ROLE);
                    if (FirestoreConstants.ROLE_ADMIN.equals(role)) {
                        onAccessGranted.run();
                    } else {
                        denyAdminAccess();
                    }
                })
                .addOnFailureListener(error -> {
                    Log.e(getLogTag(), "Failed to verify admin access", error);
                    denyAdminAccess();
                });
    }

    protected void handleAdminDataLoadFailure(String operation, Throwable error) {
        Log.e(getLogTag(), operation, error);
        if (FirestoreErrorUtils.isPermissionDenied(error)) {
            denyAdminAccess();
            return;
        }
        Toast.makeText(this, R.string.error_load_admin_data, Toast.LENGTH_SHORT).show();
    }

    protected void denyAdminAccess() {
        Toast.makeText(this, R.string.error_admin_permission_required, Toast.LENGTH_SHORT).show();
        finish();
    }

    protected String getLogTag() {
        return getClass().getSimpleName();
    }
}
