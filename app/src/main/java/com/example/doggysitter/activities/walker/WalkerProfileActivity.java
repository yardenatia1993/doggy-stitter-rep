package com.example.doggysitter.activities.walker;

import com.example.doggysitter.R;
import com.example.doggysitter.models.IsraeliLocation;
import com.example.doggysitter.models.WalkerProfile;
import com.example.doggysitter.repositories.UserRepository;
import com.example.doggysitter.repositories.WalkerProfileRepository;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.IsraeliLocationPickerDialog;
import com.example.doggysitter.utils.LocationUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkerProfileActivity extends AppCompatActivity {
    private static final String TAG = "WalkerProfileActivity";
    private static final int REQUEST_SERVICE_LOCATION_PERMISSION = 2002;
    private static final List<String> PHONE_PREFIXES = Arrays.asList(
            "050", "051", "052", "053", "054", "055", "056", "058", "059",
            "02", "03", "04", "08", "09"
    );

    private Spinner phonePrefixSpinner;
    private EditText phoneNumberEditText;
    private EditText descriptionEditText;
    private EditText experienceYearsEditText;
    private EditText estimatedHourlyPriceEditText;
    private EditText serviceRadiusEditText;
    private TextView averageRatingTextView;
    private TextView ratingCountTextView;
    private TextView noRatingsTextView;
    private TextView serviceLocationStatusTextView;
    private Button currentServiceLocationButton;
    private Button manualServiceLocationButton;
    private Button saveButton;
    private ProgressBar progressBar;
    private WalkerProfileRepository profileRepository;
    private UserRepository userRepository;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationCancellationTokenSource;
    private final List<DayAvailabilityView> dayViews = new ArrayList<>();
    private boolean profileExists;
    private boolean permissionRequestInProgress;
    private boolean locationRequestInProgress;
    private boolean availabilityTimeOrderError;
    private String currentFullName = "";
    private Double serviceLat;
    private Double serviceLng;
    private String serviceLocationLabel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        setContentView(R.layout.activity_walker_profile);

        profileRepository = new WalkerProfileRepository();
        userRepository = new UserRepository();
        initViews();
        setupPhonePrefixes();
        setupAvailabilityRows();

        currentServiceLocationButton.setOnClickListener(view -> {
            Log.d(TAG, "location button click");
            requestServiceLocation();
        });
        manualServiceLocationButton.setOnClickListener(view -> openManualServiceLocation());
        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
        Log.d(TAG, "onCreate finished");
    }

    private void initViews() {
        findViewById(R.id.main).requestFocus();
        phonePrefixSpinner = findViewById(R.id.spinner_phone_prefix);
        phoneNumberEditText = findViewById(R.id.edit_phone_number);
        descriptionEditText = findViewById(R.id.edit_description);
        experienceYearsEditText = findViewById(R.id.edit_experience_years);
        estimatedHourlyPriceEditText = findViewById(R.id.edit_estimated_hourly_price);
        serviceRadiusEditText = findViewById(R.id.edit_service_radius);
        averageRatingTextView = findViewById(R.id.text_average_rating);
        ratingCountTextView = findViewById(R.id.text_rating_count);
        noRatingsTextView = findViewById(R.id.text_no_ratings);
        serviceLocationStatusTextView = findViewById(R.id.text_service_location_status);
        currentServiceLocationButton = findViewById(R.id.button_current_service_location);
        manualServiceLocationButton = findViewById(R.id.button_manual_service_location);
        saveButton = findViewById(R.id.button_save_profile);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupPhonePrefixes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                PHONE_PREFIXES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phonePrefixSpinner.setAdapter(adapter);
    }

    private void setupAvailabilityRows() {
        dayViews.clear();
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_SUNDAY,
                getString(R.string.day_sunday),
                findViewById(R.id.check_sunday),
                findViewById(R.id.layout_sunday_hours),
                findViewById(R.id.button_sunday_start),
                findViewById(R.id.button_sunday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_MONDAY,
                getString(R.string.day_monday),
                findViewById(R.id.check_monday),
                findViewById(R.id.layout_monday_hours),
                findViewById(R.id.button_monday_start),
                findViewById(R.id.button_monday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_TUESDAY,
                getString(R.string.day_tuesday),
                findViewById(R.id.check_tuesday),
                findViewById(R.id.layout_tuesday_hours),
                findViewById(R.id.button_tuesday_start),
                findViewById(R.id.button_tuesday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_WEDNESDAY,
                getString(R.string.day_wednesday),
                findViewById(R.id.check_wednesday),
                findViewById(R.id.layout_wednesday_hours),
                findViewById(R.id.button_wednesday_start),
                findViewById(R.id.button_wednesday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_THURSDAY,
                getString(R.string.day_thursday),
                findViewById(R.id.check_thursday),
                findViewById(R.id.layout_thursday_hours),
                findViewById(R.id.button_thursday_start),
                findViewById(R.id.button_thursday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_FRIDAY,
                getString(R.string.day_friday),
                findViewById(R.id.check_friday),
                findViewById(R.id.layout_friday_hours),
                findViewById(R.id.button_friday_start),
                findViewById(R.id.button_friday_end)
        ));
        dayViews.add(new DayAvailabilityView(
                FirestoreConstants.DAY_SATURDAY,
                getString(R.string.day_saturday),
                findViewById(R.id.check_saturday),
                findViewById(R.id.layout_saturday_hours),
                findViewById(R.id.button_saturday_start),
                findViewById(R.id.button_saturday_end)
        ));

        for (DayAvailabilityView dayView : dayViews) {
            dayView.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    dayView.setVisible(isChecked)
            );
            dayView.startButton.setOnClickListener(view -> showTimePicker(dayView, true));
            dayView.endButton.setOnClickListener(view -> showTimePicker(dayView, false));
        }
    }

    private void showTimePicker(DayAvailabilityView dayView, boolean startTime) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (timePicker, hourOfDay, minute) -> {
                    String selectedTime = DateTimeUtils.formatTime(hourOfDay, minute);
                    if (startTime) {
                        dayView.setStartTime(selectedTime);
                    } else {
                        dayView.setEndTime(selectedTime);
                    }
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        dialog.show();
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
                    } else {
                        showNoRatings();
                    }
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Log.e(TAG, "loadProfile failure", error);
                    Toast.makeText(this, R.string.error_load_walker_profile, Toast.LENGTH_SHORT).show();
                });
    }

    private void populateProfile(DocumentSnapshot documentSnapshot) {
        populatePhone(documentSnapshot);
        descriptionEditText.setText(documentSnapshot.getString(FirestoreConstants.FIELD_DESCRIPTION));

        Long experienceYears = documentSnapshot.getLong(FirestoreConstants.FIELD_EXPERIENCE_YEARS);
        if (experienceYears != null) {
            experienceYearsEditText.setText(String.valueOf(experienceYears));
        }

        Long estimatedHourlyPrice = documentSnapshot.getLong(FirestoreConstants.FIELD_ESTIMATED_HOURLY_PRICE);
        if (estimatedHourlyPrice != null) {
            estimatedHourlyPriceEditText.setText(String.valueOf(estimatedHourlyPrice));
        } else {
            Double legacyPricePerWalk = documentSnapshot.getDouble(FirestoreConstants.FIELD_PRICE_PER_WALK);
            if (legacyPricePerWalk != null) {
                estimatedHourlyPriceEditText.setText(String.valueOf(Math.round(legacyPricePerWalk)));
            }
        }

        populateAvailability(documentSnapshot);

        serviceLat = documentSnapshot.getDouble(FirestoreConstants.FIELD_SERVICE_LAT);
        serviceLng = documentSnapshot.getDouble(FirestoreConstants.FIELD_SERVICE_LNG);
        serviceLocationLabel = ValidationUtils.normalizeSpaces(
                documentSnapshot.getString(FirestoreConstants.FIELD_SERVICE_LOCATION_LABEL)
        );
        if (serviceLat != null && serviceLng != null) {
            serviceLocationStatusTextView.setText(
                    TextUtils.isEmpty(serviceLocationLabel)
                            ? getString(R.string.service_location_saved)
                            : serviceLocationLabel
            );
        }

        Object serviceRadiusValue = documentSnapshot.get(FirestoreConstants.FIELD_SERVICE_RADIUS_KM);
        if (serviceRadiusValue instanceof Number) {
            serviceRadiusEditText.setText(String.valueOf(((Number) serviceRadiusValue).intValue()));
        }

        populateRatingSummary(documentSnapshot);
    }

    private void populateRatingSummary(DocumentSnapshot documentSnapshot) {
        long ratingCount = getLongNumber(documentSnapshot, FirestoreConstants.FIELD_RATING_COUNT);
        if (ratingCount <= 0) {
            showNoRatings();
            return;
        }

        Double averageRating = getDoubleNumber(documentSnapshot, FirestoreConstants.FIELD_AVERAGE_RATING);
        if (averageRating == null) {
            long ratingSum = getLongNumber(documentSnapshot, FirestoreConstants.FIELD_RATING_SUM);
            averageRating = (double) ratingSum / ratingCount;
        }

        noRatingsTextView.setVisibility(View.GONE);
        averageRatingTextView.setVisibility(View.VISIBLE);
        ratingCountTextView.setVisibility(View.VISIBLE);
        averageRatingTextView.setText(getString(R.string.average_rating, averageRating));
        ratingCountTextView.setText(getString(R.string.rating_count, ratingCount));
    }

    private void showNoRatings() {
        averageRatingTextView.setVisibility(View.GONE);
        ratingCountTextView.setVisibility(View.GONE);
        noRatingsTextView.setVisibility(View.VISIBLE);
    }

    private long getLongNumber(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private Double getDoubleNumber(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private void populatePhone(DocumentSnapshot documentSnapshot) {
        String prefix = documentSnapshot.getString(FirestoreConstants.FIELD_PHONE_PREFIX);
        String number = documentSnapshot.getString(FirestoreConstants.FIELD_PHONE_NUMBER);
        String fullPhone = documentSnapshot.getString(FirestoreConstants.FIELD_PHONE);

        if (TextUtils.isEmpty(prefix) || TextUtils.isEmpty(number)) {
            String[] parsedPhone = parseLegacyPhone(fullPhone);
            prefix = parsedPhone[0];
            number = parsedPhone[1];
        }

        int prefixIndex = PHONE_PREFIXES.indexOf(prefix);
        if (prefixIndex >= 0) {
            phonePrefixSpinner.setSelection(prefixIndex);
        }
        phoneNumberEditText.setText(number);
    }

    private String[] parseLegacyPhone(String fullPhone) {
        if (TextUtils.isEmpty(fullPhone)) {
            return new String[]{"050", ""};
        }
        for (String prefix : PHONE_PREFIXES) {
            if (fullPhone.startsWith(prefix)) {
                return new String[]{prefix, fullPhone.substring(prefix.length())};
            }
        }
        return new String[]{"050", fullPhone};
    }

    private void populateAvailability(DocumentSnapshot documentSnapshot) {
        Object availabilityValue = documentSnapshot.get(FirestoreConstants.FIELD_AVAILABILITY);
        if (availabilityValue instanceof Map<?, ?>) {
            Map<?, ?> availability = (Map<?, ?>) availabilityValue;
            for (DayAvailabilityView dayView : dayViews) {
                Object dayValue = availability.get(dayView.firestoreKey);
                if (dayValue instanceof Map<?, ?>) {
                    Map<?, ?> dayAvailability = (Map<?, ?>) dayValue;
                    String start = getStringValue(dayAvailability.get(FirestoreConstants.FIELD_AVAILABILITY_START));
                    String end = getStringValue(dayAvailability.get(FirestoreConstants.FIELD_AVAILABILITY_END));
                    dayView.setChecked(true);
                    dayView.setStartTime(start);
                    dayView.setEndTime(end);
                }
            }
            return;
        }

        Object availableDaysValue = documentSnapshot.get(FirestoreConstants.FIELD_AVAILABLE_DAYS);
        if (availableDaysValue instanceof List<?>) {
            List<?> availableDays = (List<?>) availableDaysValue;
            for (DayAvailabilityView dayView : dayViews) {
                dayView.setChecked(availableDays.contains(dayView.hebrewLabel));
            }
        }
    }

    private String getStringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private void saveProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String phonePrefix = getSelectedPhonePrefix();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String description = ValidationUtils.normalizeSpaces(descriptionEditText.getText().toString());
        String experienceYearsText = experienceYearsEditText.getText().toString().trim();
        String estimatedHourlyPriceText = estimatedHourlyPriceEditText.getText().toString().trim();
        String serviceRadiusText = serviceRadiusEditText.getText().toString().trim();
        List<String> availableDays = getSelectedDays();

        if (TextUtils.isEmpty(phonePrefix)) {
            Toast.makeText(this, R.string.error_phone_prefix_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError(getString(R.string.error_phone_number_required));
            return;
        }
        if (!phoneNumber.matches("^\\d+$")) {
            phoneNumberEditText.setError(getString(R.string.error_phone_digits_only));
            return;
        }
        if (phoneNumber.length() != 7) {
            phoneNumberEditText.setError(getString(R.string.error_phone_number_length));
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

        Integer estimatedHourlyPrice = parseWholeNumber(
                estimatedHourlyPriceText,
                estimatedHourlyPriceEditText,
                R.string.error_estimated_hourly_price_required,
                R.string.error_estimated_hourly_price_numeric
        );
        if (estimatedHourlyPrice == null) {
            return;
        }
        if (estimatedHourlyPrice <= 0 || estimatedHourlyPrice > 500) {
            estimatedHourlyPriceEditText.setError(getString(R.string.error_estimated_hourly_price_range));
            return;
        }
        if (availableDays.isEmpty()) {
            Toast.makeText(this, R.string.error_available_days_required, Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Map<String, String>> availability = getAvailability();
        if (availability == null) {
            if (!availabilityTimeOrderError) {
                Toast.makeText(this, R.string.error_availability_hours_required, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (serviceLat == null || serviceLng == null) {
            Toast.makeText(this, R.string.error_service_location_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer serviceRadiusKm = parseWholeNumber(
                serviceRadiusText,
                serviceRadiusEditText,
                R.string.error_service_radius_required,
                R.string.error_service_radius_numeric
        );
        if (serviceRadiusKm == null) {
            return;
        }
        if (serviceRadiusKm < 1 || serviceRadiusKm > 50) {
            serviceRadiusEditText.setError(getString(R.string.error_service_radius_range));
            return;
        }

        WalkerProfile profile = new WalkerProfile(
                currentUser.getUid(),
                currentFullName,
                phonePrefix + phoneNumber,
                description,
                experienceYears,
                null,
                availableDays,
                true
        );
        profile.setPhonePrefix(phonePrefix);
        profile.setPhoneNumber(phoneNumber);
        profile.setEstimatedHourlyPrice(estimatedHourlyPrice);
        profile.setAvailability(availability);
        profile.setServiceLat(serviceLat);
        profile.setServiceLng(serviceLng);
        profile.setServiceRadiusKm(serviceRadiusKm);
        profile.setServiceLocationLabel(serviceLocationLabel);

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
                    Log.e(TAG, "saveProfile failure", error);
                    Toast.makeText(this, R.string.error_save_walker_profile, Toast.LENGTH_SHORT).show();
                });
    }

    private Integer parseWholeNumber(String value, EditText editText, int requiredErrorResId,
                                     int numericErrorResId) {
        if (TextUtils.isEmpty(value)) {
            editText.setError(getString(requiredErrorResId));
            return null;
        }
        if (!value.matches("^\\d+$")) {
            editText.setError(getString(numericErrorResId));
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            editText.setError(getString(numericErrorResId));
            return null;
        }
    }

    private String getSelectedPhonePrefix() {
        Object selectedItem = phonePrefixSpinner.getSelectedItem();
        return selectedItem == null ? "" : selectedItem.toString();
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        for (DayAvailabilityView dayView : dayViews) {
            if (dayView.checkBox.isChecked()) {
                selectedDays.add(dayView.hebrewLabel);
            }
        }
        return selectedDays;
    }

    private Map<String, Map<String, String>> getAvailability() {
        availabilityTimeOrderError = false;
        Map<String, Map<String, String>> availability = new HashMap<>();
        for (DayAvailabilityView dayView : dayViews) {
            if (!dayView.checkBox.isChecked()) {
                continue;
            }
            if (TextUtils.isEmpty(dayView.startTime) || TextUtils.isEmpty(dayView.endTime)) {
                return null;
            }
            if (dayView.endTime.compareTo(dayView.startTime) <= 0) {
                availabilityTimeOrderError = true;
                Toast.makeText(this, R.string.error_availability_end_after_start, Toast.LENGTH_SHORT).show();
                return null;
            }
            Map<String, String> timeRange = new HashMap<>();
            timeRange.put(FirestoreConstants.FIELD_AVAILABILITY_START, dayView.startTime);
            timeRange.put(FirestoreConstants.FIELD_AVAILABILITY_END, dayView.endTime);
            availability.put(dayView.firestoreKey, timeRange);
        }
        return availability;
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
        serviceLocationLabel = "";
        Log.d(TAG, "location success");
        serviceLocationStatusTextView.setText(R.string.service_location_saved);
        Toast.makeText(this, R.string.service_location_saved, Toast.LENGTH_SHORT).show();
    }

    private void openManualServiceLocation() {
        IsraeliLocationPickerDialog.show(this, this::handleManualServiceLocation);
    }

    private void handleManualServiceLocation(IsraeliLocation location) {
        serviceLat = location.getLat();
        serviceLng = location.getLng();
        serviceLocationLabel = location.getNameHe();
        serviceLocationStatusTextView.setText(getString(
                R.string.location_saved_with_name,
                location.getNameHe()
        ));
        Toast.makeText(
                this,
                getString(R.string.location_saved_with_name, location.getNameHe()),
                Toast.LENGTH_SHORT
        ).show();
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
        manualServiceLocationButton.setEnabled(!loading);
    }

    private static class DayAvailabilityView {
        private final String firestoreKey;
        private final String hebrewLabel;
        private final CheckBox checkBox;
        private final LinearLayout timeRangeLayout;
        private final Button startButton;
        private final Button endButton;
        private String startTime = "";
        private String endTime = "";

        private DayAvailabilityView(String firestoreKey, String hebrewLabel, CheckBox checkBox,
                                    LinearLayout timeRangeLayout, Button startButton,
                                    Button endButton) {
            this.firestoreKey = firestoreKey;
            this.hebrewLabel = hebrewLabel;
            this.checkBox = checkBox;
            this.timeRangeLayout = timeRangeLayout;
            this.startButton = startButton;
            this.endButton = endButton;
        }

        private void setChecked(boolean checked) {
            checkBox.setChecked(checked);
            setVisible(checked);
        }

        private void setVisible(boolean visible) {
            timeRangeLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        private void setStartTime(String startTime) {
            this.startTime = startTime == null ? "" : startTime;
            if (!TextUtils.isEmpty(this.startTime)) {
                startButton.setText(this.startTime);
            }
        }

        private void setEndTime(String endTime) {
            this.endTime = endTime == null ? "" : endTime;
            if (!TextUtils.isEmpty(this.endTime)) {
                endButton.setText(this.endTime);
            }
        }
    }
}
