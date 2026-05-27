package com.example.doggysitter.activities.auth;

import com.example.doggysitter.R;
import com.example.doggysitter.activities.owner.OwnerDashboardActivity;
import com.example.doggysitter.activities.walker.WalkerDashboardActivity;
import com.example.doggysitter.repositories.AuthRepository;
import com.example.doggysitter.repositories.UserRepository;
import com.example.doggysitter.utils.FirestoreConstants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class RoleSelectionActivity extends AppCompatActivity {
    private Button ownerButton;
    private Button walkerButton;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        initViews();

        ownerButton.setOnClickListener(view -> saveRole(FirestoreConstants.ROLE_OWNER));
        walkerButton.setOnClickListener(view -> saveRole(FirestoreConstants.ROLE_WALKER));
    }

    private void initViews() {
        ownerButton = findViewById(R.id.button_owner);
        walkerButton = findViewById(R.id.button_walker);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void saveRole(String role) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            openAndFinish(LoginActivity.class);
            return;
        }

        setLoading(true);
        userRepository.saveRole(currentUser.getUid(), role)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    routeByRole(role);
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_save_role, Toast.LENGTH_SHORT).show();
                });
    }

    private void routeByRole(String role) {
        if (FirestoreConstants.ROLE_OWNER.equals(role)) {
            openAndFinish(OwnerDashboardActivity.class);
        } else {
            openAndFinish(WalkerDashboardActivity.class);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        ownerButton.setEnabled(!loading);
        walkerButton.setEnabled(!loading);
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
