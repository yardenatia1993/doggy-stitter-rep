package com.example.doggysitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button btnRegister;
    ProgressBar progressBar;
    private UserRepository userRepository;
    private FirebaseAuthManager authManager;
    TextView goToLogin;
    RadioButton radioOwner, radioWalker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitViews();
        authManager=new FirebaseAuthManager();
        userRepository=new UserRepository();
        RegisterHandler();
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });


    }


    public void RegisterHandler() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = String.valueOf(editTextEmail.getText()).trim();
                String password = String.valueOf(editTextPassword.getText()).trim();
                if (!InputValidatorUtil.isValidInput(RegisterActivity.this, email, password)) return;

                if (!radioOwner.isChecked() && !radioWalker.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "Please select user type", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                authManager.register(email, password, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = authManager.getCurrentUser();
                        assert firebaseUser != null;

                        if (radioWalker.isChecked()) {
                            // Create a UserWalker object
                            UserWalker walker = new UserWalker(firebaseUser.getUid(), email);
                            userRepository.addUser(walker, aVoid -> {
                                Toast.makeText(RegisterActivity.this, "Dog Walker registered successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }, e -> {
                                Toast.makeText(RegisterActivity.this, "Failed to save Dog Walker data", Toast.LENGTH_SHORT).show();
                            });
                        } else if (radioOwner.isChecked()) {
                            // Create a UserOwner object
                            UserOwner owner = new UserOwner(firebaseUser.getUid(), email);
                            userRepository.addUser(owner, aVoid -> {
                                Toast.makeText(RegisterActivity.this, "Dog Owner registered successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }, e -> {
                                Toast.makeText(RegisterActivity.this, "Failed to save Dog Owner data", Toast.LENGTH_SHORT).show();
                            });
                        }

                    } else {
                        // Handle registration failure
                        Toast.makeText(RegisterActivity.this, "Authentication failed:\n"
                                        + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void InitViews() {
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        goToLogin = findViewById(R.id.login);
        radioOwner = findViewById(R.id.radio_owner);
        radioWalker = findViewById(R.id.radio_walker);

    }
}




