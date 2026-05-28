package com.example.doggysitter.activities.auth;

import com.example.doggysitter.R;
import com.example.doggysitter.models.User;
import com.example.doggysitter.repositories.AuthRepository;
import com.example.doggysitter.repositories.UserRepository;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private Button loginButton;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        initViews();

        registerButton.setOnClickListener(view -> register());
        loginButton.setOnClickListener(view -> openAndFinish(LoginActivity.class));
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.edit_full_name);
        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        confirmPasswordEditText = findViewById(R.id.edit_confirm_password);
        registerButton = findViewById(R.id.button_register);
        loginButton = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void register() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!validateInput(fullName, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);
        authRepository.register(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setLoading(false);
                Exception error = task.getException();
                Log.e(TAG, "Registration failed", error);
                Toast.makeText(this, getRegisterErrorMessageResId(error), Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser firebaseUser = authRepository.getCurrentUser();
            if (firebaseUser == null) {
                setLoading(false);
                Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User(firebaseUser.getUid(), fullName, email, null);
            userRepository.createUser(user)
                    .addOnSuccessListener(unused -> {
                        setLoading(false);
                        openAndFinish(RoleSelectionActivity.class);
                    })
                    .addOnFailureListener(error -> {
                        setLoading(false);
                        Log.e(TAG, "Failed to create user profile after registration", error);
                        Toast.makeText(this, R.string.error_save_profile, Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError(getString(R.string.error_full_name_required));
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordEditText.setError(getString(R.string.error_password_too_short));
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_confirm_password_required));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_passwords_do_not_match));
            return false;
        }
        return true;
    }

    private int getRegisterErrorMessageResId(Exception error) {
        if (error instanceof FirebaseAuthUserCollisionException) {
            return R.string.error_email_already_exists;
        }
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return R.string.error_weak_password;
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return R.string.error_invalid_email;
        }
        if (hasNetworkError(error)) {
            return R.string.error_network_connection;
        }
        return R.string.error_register_failed;
    }

    private boolean hasNetworkError(Throwable error) {
        while (error != null) {
            if (error instanceof FirebaseNetworkException) {
                return true;
            }
            error = error.getCause();
        }
        return false;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        loginButton.setEnabled(!loading);
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

