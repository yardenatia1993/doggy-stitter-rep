package com.example.doggysitter.activities.auth;

import com.example.doggysitter.R;
import com.example.doggysitter.activities.admin.AdminDashboardActivity;
import com.example.doggysitter.activities.owner.OwnerDashboardActivity;
import com.example.doggysitter.activities.walker.WalkerDashboardActivity;
import com.example.doggysitter.repositories.AuthRepository;
import com.example.doggysitter.repositories.UserRepository;
import com.example.doggysitter.utils.FirestoreConstants;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            openAndFinish(LoginActivity.class);
            return;
        }

        loadUserRole(currentUser.getUid());
    }

    private void loadUserRole(String uid) {
        userRepository.getUser(uid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        openAndFinish(RoleSelectionActivity.class);
                        return;
                    }

                    String role = documentSnapshot.getString(FirestoreConstants.FIELD_ROLE);
                    routeByRole(role);
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, R.string.error_load_profile, Toast.LENGTH_SHORT).show();
                    openAndFinish(LoginActivity.class);
                });
    }

    private void routeByRole(String role) {
        if (FirestoreConstants.ROLE_OWNER.equals(role)) {
            openAndFinish(OwnerDashboardActivity.class);
        } else if (FirestoreConstants.ROLE_WALKER.equals(role)) {
            openAndFinish(WalkerDashboardActivity.class);
        } else if (FirestoreConstants.ROLE_ADMIN.equals(role)) {
            openAndFinish(AdminDashboardActivity.class);
        } else {
            openAndFinish(RoleSelectionActivity.class);
        }
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
