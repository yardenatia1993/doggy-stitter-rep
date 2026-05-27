package com.example.doggysitter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalkerProfileActivity extends AppCompatActivity {
    private static final String TAG = "WalkerProfileActivity";
    private static final int REQUEST_SERVICE_LOCATION_PERMISSION = 2002;

    private EditText phoneEditText;
    private EditText descriptionEditText;
    private EditText experienceYearsEditText;
    private EditText pricePerWalkEditText;
    private EditText serviceRadiusEditText;
    private TextView serviceLocationStatusTextView;
    private Button currentServiceLocationButton;
    private Button saveButton;
    private ProgressBar progressBar;
    private WalkerProfileRepository profileRepository;
    private UserRepository userRepository;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationCancellationTokenSource;
    private List<CheckBox> dayCheckBoxes;
    private boolean profileExists;
    private boolean permissionRequestInProgress;
    private boolean locationRequestInProgress;
    private String currentFullName = "";
    private Double serviceLat;
    private Double serviceLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        setContentView(R.layout.activity_walker_profile);

        profileRepository = new WalkerProfileRepository();
        userRepository = new UserRepository();
        initViews();

        currentServiceLocationButton.setOnClickListener(view -> {
            Log.d(TAG, "location button click");
            requestServiceLocation();
        });
        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
        Log.d(TAG, "onCreate finished");
    }

    private void initViews() {
        findViewById(R.id.main).requestFocus();
        phoneEditText = findViewById(R.id.edit_phone);
        descriptionEditText = findViewById(R.id.edit_description);
        experienceYearsEditText = findViewById(R.id.edit_experience_years);
        pricePerWalkEditText = findViewById(R.id.edit_price_per_walk);
        serviceRadiusEditText = findViewById(R.id.edit_service_radius);
        serviceLocationStatusTextView = findViewById(R.id.text_service_location_status);
        currentServiceLocationButton = findViewById(R.id.button_current_service_location);
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
            Log.d(TAG, "loadProfile aborted: no current user");
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "loadProfile start");
        setLoading(true);
        userRepository.getUser(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    currentFullName = documentSnapshot.getString(FirestoreConstants.FIELD_FULL_NAME);
                    Log.d(TAG, "user name load success");
                })
                .addOnFailureListener(error -> {
                    currentFullName = "";
                    Log.e(TAG, "user name load failure", error);
                });

        profileRepository.getProfile(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    profileExists = documentSnapshot.exists();
                    Log.d(TAG, "loadProfile success, exists=" + profileExists);
                    if (profileExists) {
                        populateProfile(documentSnapshot);
                    }
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Log.e(TAG, "loadProfile failure", error);
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

        serviceLat = documentSnapshot.getDouble(FirestoreConstants.FIELD_SERVICE_LAT);
        serviceLng = documentSnapshot.getDouble(FirestoreConstants.FIELD_SERVICE_LNG);
        if (serviceLat != null && serviceLng != null) {
            serviceLocationStatusTextView.setText(R.string.service_location_saved);
        }

        Object serviceRadiusValue = documentSnapshot.get(FirestoreConstants.FIELD_SERVICE_RADIUS_KM);
        if (serviceRadiusValue instanceof Number) {
            double serviceRadius = ((Number) serviceRadiusValue).doubleValue();
            serviceRadiusEditText.setText(String.valueOf(serviceRadius));
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
        String serviceRadiusText = serviceRadiusEditText.getText().toString().trim();
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
        if (serviceLat == null || serviceLng == null) {
            Toast.makeText(this, R.string.error_service_location_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(serviceRadiusText)) {
            serviceRadiusEditText.setError(getString(R.string.error_service_radius_required));
            return;
        }

        double serviceRadiusKm;
        try {
            serviceRadiusKm = Double.parseDouble(serviceRadiusText);
        } catch (NumberFormatException error) {
            serviceRadiusEditText.setError(getString(R.string.error_service_radius_numeric));
            return;
        }
        if (serviceRadiusKm < 1 || serviceRadiusKm > 50) {
            serviceRadiusEditText.setError(getString(R.string.error_service_radius_range));
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
        profile.setServiceLat(serviceLat);
        profile.setServiceLng(serviceLng);
        profile.setServiceRadiusKm(serviceRadiusKm);

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

    private void requestServiceLocation() {
        if (locationRequestInProgress || permissionRequestInProgress) {
            Log.d(TAG, "location request ignored: request already in progress");
            return;
        }
        if (!LocationUtils.hasLocationPermission(this)) {
            Log.d(TAG, "requesting location permission");
            permissionRequestInProgress = true;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_SERVICE_LOCATION_PERMISSION
            );
            return;
        }
        loadCurrentServiceLocation();
    }

    private void loadCurrentServiceLocation() {
        if (!LocationUtils.hasLocationPermission(this)) {
            Log.d(TAG, "loadCurrentServiceLocation blocked: missing permission");
            Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationRequestInProgress) {
            Log.d(TAG, "loadCurrentServiceLocation ignored: already in progress");
            return;
        }

        Log.d(TAG, "location load start");
        locationRequestInProgress = true;
        setLoading(true);
        try {
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            }
            locationCancellationTokenSource = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            locationCancellationTokenSource.getToken()
                    )
                    .addOnSuccessListener(this, this::handleServiceLocation)
                    .addOnFailureListener(this, error -> {
                        finishLocationRequest();
                        Log.e(TAG, "location failure", error);
                        Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException error) {
            finishLocationRequest();
            Log.e(TAG, "location permission failure", error);
            Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleServiceLocation(Location location) {
        finishLocationRequest();
        if (location == null) {
            Log.d(TAG, "location success callback returned null location");
            Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        serviceLat = location.getLatitude();
        serviceLng = location.getLongitude();
        Log.d(TAG, "location success");
        serviceLocationStatusTextView.setText(R.string.service_location_saved);
        Toast.makeText(this, R.string.service_location_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_SERVICE_LOCATION_PERMISSION) {
            return;
        }

        permissionRequestInProgress = false;
        boolean granted = false;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        Log.d(TAG, "permission result granted=" + granted);
        if (granted) {
            loadCurrentServiceLocation();
        } else {
            Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (locationCancellationTokenSource != null) {
            locationCancellationTokenSource.cancel();
            locationCancellationTokenSource = null;
        }
        super.onDestroy();
    }

    private void finishLocationRequest() {
        locationRequestInProgress = false;
        locationCancellationTokenSource = null;
        setLoading(false);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
        currentServiceLocationButton.setEnabled(!loading);
    }
}
