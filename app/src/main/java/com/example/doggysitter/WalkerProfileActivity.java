package com.example.doggysitter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalkerProfileActivity extends AppCompatActivity {
    private EditText phoneEditText;
    private EditText descriptionEditText;
    private EditText experienceYearsEditText;
    private EditText pricePerWalkEditText;
    private Button saveButton;
    private ProgressBar progressBar;
    private WalkerProfileRepository profileRepository;
    private UserRepository userRepository;
    private List<CheckBox> dayCheckBoxes;
    private boolean profileExists;
    private String currentFullName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_profile);

        profileRepository = new WalkerProfileRepository();
        userRepository = new UserRepository();
        initViews();

        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void initViews() {
        phoneEditText = findViewById(R.id.edit_phone);
        descriptionEditText = findViewById(R.id.edit_description);
        experienceYearsEditText = findViewById(R.id.edit_experience_years);
        pricePerWalkEditText = findViewById(R.id.edit_price_per_walk);
        saveButton = findViewById(R.id.button_save_profile);
        progressBar = findViewById(R.id.progress_bar);
        dayCheckBoxes = Arrays.asList(
                findViewById(R.id.check_sunday),
                findViewById(R.id.check_monday),
                findViewById(R.id.check_tuesday),
                findViewById(R.id.check_wednesday),
                findViewById(R.id.check_thursday),
                findViewById(R.id.check_friday),
                findViewById(R.id.check_saturday)
        );
    }

    private void loadProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        userRepository.getUser(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot ->
                        currentFullName = documentSnapshot.getString(FirestoreConstants.FIELD_FULL_NAME))
                .addOnFailureListener(error -> currentFullName = "");

        profileRepository.getProfile(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    profileExists = documentSnapshot.exists();
                    if (profileExists) {
                        populateProfile(documentSnapshot);
                    }
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_walker_profile, Toast.LENGTH_SHORT).show();
                });
    }

    private void populateProfile(DocumentSnapshot documentSnapshot) {
        phoneEditText.setText(documentSnapshot.getString(FirestoreConstants.FIELD_PHONE));
        descriptionEditText.setText(documentSnapshot.getString(FirestoreConstants.FIELD_DESCRIPTION));

        Long experienceYears = documentSnapshot.getLong(FirestoreConstants.FIELD_EXPERIENCE_YEARS);
        if (experienceYears != null) {
            experienceYearsEditText.setText(String.valueOf(experienceYears));
        }

        Double pricePerWalk = documentSnapshot.getDouble(FirestoreConstants.FIELD_PRICE_PER_WALK);
        if (pricePerWalk != null) {
            pricePerWalkEditText.setText(String.valueOf(pricePerWalk));
        }

        Object availableDaysValue = documentSnapshot.get(FirestoreConstants.FIELD_AVAILABLE_DAYS);
        if (availableDaysValue instanceof List<?>) {
            List<?> availableDays = (List<?>) availableDaysValue;
            for (CheckBox checkBox : dayCheckBoxes) {
                checkBox.setChecked(availableDays.contains(checkBox.getText().toString()));
            }
        }
    }

    private void saveProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String phone = ValidationUtils.normalizeSpaces(phoneEditText.getText().toString());
        String description = ValidationUtils.normalizeSpaces(descriptionEditText.getText().toString());
        String experienceYearsText = experienceYearsEditText.getText().toString().trim();
        String pricePerWalkText = pricePerWalkEditText.getText().toString().trim();
        List<String> availableDays = getSelectedDays();

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError(getString(R.string.error_phone_required));
            return;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError(getString(R.string.error_description_required));
            return;
        }
        if (TextUtils.isEmpty(experienceYearsText)) {
            experienceYearsEditText.setError(getString(R.string.error_experience_required));
            return;
        }

        int experienceYears;
        try {
            experienceYears = Integer.parseInt(experienceYearsText);
        } catch (NumberFormatException error) {
            experienceYearsEditText.setError(getString(R.string.error_experience_numeric));
            return;
        }
        if (experienceYears < 0 || experienceYears > 60) {
            experienceYearsEditText.setError(getString(R.string.error_experience_range));
            return;
        }

        if (TextUtils.isEmpty(pricePerWalkText)) {
            pricePerWalkEditText.setError(getString(R.string.error_price_per_walk_required));
            return;
        }
        if (!ValidationUtils.isPositivePrice(pricePerWalkText)) {
            pricePerWalkEditText.setError(getString(R.string.error_price_per_walk_positive));
            return;
        }
        if (availableDays.isEmpty()) {
            Toast.makeText(this, R.string.error_available_days_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerWalk = Double.parseDouble(pricePerWalkText);
        WalkerProfile profile = new WalkerProfile(
                currentUser.getUid(),
                currentFullName,
                phone,
                description,
                experienceYears,
                pricePerWalk,
                availableDays,
                true
        );

        setLoading(true);
        profileRepository.saveProfile(profile, profileExists)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    profileExists = true;
                    Toast.makeText(this, R.string.walker_profile_saved, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_save_walker_profile, Toast.LENGTH_SHORT).show();
                });
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        for (CheckBox checkBox : dayCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedDays.add(checkBox.getText().toString());
            }
        }
        return selectedDays;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }
}
