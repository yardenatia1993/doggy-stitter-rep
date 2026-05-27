package com.example.doggysitter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WalkerDashboardActivity extends AppCompatActivity {
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_dashboard);

        authRepository = new AuthRepository();
        Button logoutButton = findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(view -> logout());
    }

    private void logout() {
        authRepository.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
