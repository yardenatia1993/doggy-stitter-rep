package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.activities.auth.LoginActivity;
import com.example.doggysitter.repositories.AuthRepository;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OwnerDashboardActivity extends AppCompatActivity {
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        authRepository = new AuthRepository();
        Button addDogButton = findViewById(R.id.button_add_dog);
        Button myDogsButton = findViewById(R.id.button_my_dogs);
        Button createWalkRequestButton = findViewById(R.id.button_create_walk_request);
        Button myRequestsButton = findViewById(R.id.button_my_requests);
        Button logoutButton = findViewById(R.id.button_logout);

        addDogButton.setOnClickListener(view -> startActivity(new Intent(this, AddDogActivity.class)));
        myDogsButton.setOnClickListener(view -> startActivity(new Intent(this, MyDogsActivity.class)));
        createWalkRequestButton.setOnClickListener(
                view -> startActivity(new Intent(this, CreateWalkRequestActivity.class))
        );
        myRequestsButton.setOnClickListener(view -> startActivity(new Intent(this, OwnerRequestsActivity.class)));
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
