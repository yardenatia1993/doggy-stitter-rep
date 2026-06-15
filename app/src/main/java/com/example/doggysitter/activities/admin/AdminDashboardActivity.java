package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.activities.auth.LoginActivity;
import com.example.doggysitter.repositories.AuthRepository;
import com.example.doggysitter.utils.ExitConfirmationUtils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class AdminDashboardActivity extends AdminBaseActivity {
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        authRepository = new AuthRepository();
        ExitConfirmationUtils.install(this);
        Button usersButton = findViewById(R.id.button_users);
        Button walkRequestsButton = findViewById(R.id.button_walk_requests);
        Button reviewsButton = findViewById(R.id.button_reviews);
        Button statisticsButton = findViewById(R.id.button_statistics);
        Button logoutButton = findViewById(R.id.button_logout);

        setNavigationEnabled(
                false,
                usersButton,
                walkRequestsButton,
                reviewsButton,
                statisticsButton
        );
        usersButton.setOnClickListener(view -> startActivity(new Intent(this, AdminUsersActivity.class)));
        walkRequestsButton.setOnClickListener(
                view -> startActivity(new Intent(this, AdminWalkRequestsActivity.class))
        );
        reviewsButton.setOnClickListener(view -> startActivity(new Intent(this, AdminReviewsActivity.class)));
        statisticsButton.setOnClickListener(view -> startActivity(new Intent(this, AdminStatsActivity.class)));
        logoutButton.setOnClickListener(view -> logout());
        requireAdminAccess(() -> setNavigationEnabled(
                true,
                usersButton,
                walkRequestsButton,
                reviewsButton,
                statisticsButton
        ));
    }

    private void setNavigationEnabled(boolean enabled, Button... buttons) {
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }

    private void logout() {
        authRepository.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
