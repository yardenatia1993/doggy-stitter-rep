package com.example.doggysitter.activities.auth;

import com.example.doggysitter.R;
import com.example.doggysitter.repositories.AuthRepository;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository();
        initViews();

        loginButton.setOnClickListener(view -> login());
        registerButton.setOnClickListener(view -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void initViews() {
        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.button_login);
        registerButton = findViewById(R.id.button_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        setLoading(true);
        authRepository.login(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                openAndFinish(SplashScreenActivity.class);
            } else {
                Exception error = task.getException();
                Log.e(TAG, "Login failed", error);
                Toast.makeText(this, getLoginErrorMessageResId(error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getLoginErrorMessageResId(Exception error) {
        if (hasNetworkError(error)) {
            return R.string.error_network_connection;
        }
        return R.string.error_login_failed;
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
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
    }

    private void openAndFinish(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
