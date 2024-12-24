package com.example.doggysitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button btnLogin;
    ProgressBar progressBar;
    TextView goToRegister;
    private FirebaseAuthManager authManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitViews();
        goToRegister();
        LoginHandler();
    }
    public void LoginHandler(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());
                if (!InputValidatorUtil.isValidInput(LoginActivity.this, email, password))return;

                progressBar.setVisibility(View.VISIBLE);
                authManager = new FirebaseAuthManager();
                authManager.login(email, password, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = authManager.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        // Fetch the user's details from Firestore
                        UserRepository userRepository = new UserRepository();
                        userRepository.getUser(userId, documentSnapshot -> {
                            User user = documentSnapshot.toObject(User.class);
                            CurrentUser.getInstance().setUser(user);

                            // Redirect based on user type
                            assert user != null;
                            if (user.getUserType().equals("walker")) {
                               startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            } else {
                               startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                            }
                            finish();
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void InitViews() {
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        goToRegister =findViewById(R.id.register);
    }
    public void goToRegister() {
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }
}