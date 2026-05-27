package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Dog;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.DogRepository;
import com.example.doggysitter.repositories.WalkRequestRepository;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.LocationUtils;
import com.example.doggysitter.utils.PlacesLocationHelper;
import com.example.doggysitter.utils.ValidationUtils;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateWalkRequestActivity extends AppCompatActivity {
    private static final int REQUEST_PICKUP_LOCATION_PERMISSION = 2001;
    private static final int REQUEST_PICKUP_PLACE = 3001;

    private Spinner dogSpinner;
    private EditText dateEditText;
    private EditText timeEditText;
    private Spinner durationSpinner;
    private EditText maxPriceEditText;
    private EditText pickupLocationLabelEditText;
    private EditText notesEditText;
    private TextView pickupLocationStatusTextView;
    private Button currentLocationButton;
    private Button manualLocationButton;
    private Button createButton;
    private ProgressBar progressBar;
    private DogRepository dogRepository;
    private WalkRequestRepository walkRequestRepository;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationCancellationTokenSource;
    private final List<Dog> dogs = new ArrayList<>();
    private final List<Integer> durationValues = new ArrayList<>();
    private String selectedDateIso;
    private String selectedTime;
    private Double pickupLat;
    private Double pickupLng;
    private boolean permissionRequestInProgress;
    private boolean locationRequestInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_walk_request);

        dogRepository = new DogRepository();
        walkRequestRepository = new WalkRequestRepository();
        initViews();
        setupDurationSpinner();

        dateEditText.setOnClickListener(view -> showDatePicker());
        timeEditText.setOnClickListener(view -> showTimePicker());
        currentLocationButton.setOnClickListener(view -> requestPickupLocation());
        manualLocationButton.setOnClickListener(view -> openManualPickupLocation());
        createButton.setOnClickListener(view -> createWalkRequest());
        loadDogs();
    }

    private void initViews() {
        dogSpinner = findViewById(R.id.spinner_dogs);
        dateEditText = findViewById(R.id.edit_date);
        timeEditText = findViewById(R.id.edit_time);
        durationSpinner = findViewById(R.id.spinner_duration);
        maxPriceEditText = findViewById(R.id.edit_max_price);
        pickupLocationLabelEditText = findViewById(R.id.edit_pickup_location_label);
        notesEditText = findViewById(R.id.edit_notes);
        pickupLocationStatusTextView = findViewById(R.id.text_pickup_location_status);
        currentLocationButton = findViewById(R.id.button_current_pickup_location);
        manualLocationButton = findViewById(R.id.button_manual_pickup_location);
        createButton = findViewById(R.id.button_create_request);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupDurationSpinner() {
        List<String> durationLabels = new ArrayList<>();
        durationValues.add(0);
        durationLabels.add(getString(R.string.select_duration));

        for (int minutes = 15; minutes <= 300; minutes += 15) {
            durationValues.add(minutes);
            durationLabels.add(minutes + " דקות");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                durationLabels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar initialDate = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    selectedDateIso = DateTimeUtils.toIsoDate(selectedDate);
                    dateEditText.setText(DateTimeUtils.formatDisplayDate(selectedDateIso));
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);

        Calendar maxDate = (Calendar) minDate.clone();
        maxDate.add(Calendar.YEAR, 1);

        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (timePicker, hourOfDay, minute) -> {
                    selectedTime = DateTimeUtils.formatTime(hourOfDay, minute);
                    timeEditText.setText(selectedTime);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void loadDogs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        dogRepository.getDogsForOwner(currentUser.getUid())
                .addOnSuccessListener(querySnapshot -> {
                    dogs.clear();
                    List<String> dogNames = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        Dog dog = documentSnapshot.toObject(Dog.class);
                        if (dog != null) {
                            if (dog.getId() == null || dog.getId().trim().isEmpty()) {
                                dog.setId(documentSnapshot.getId());
                            }
                            dogs.add(dog);
                            dogNames.add(dog.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            dogNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dogSpinner.setAdapter(adapter);

                    setLoading(false);
                    createButton.setEnabled(!dogs.isEmpty());
                    if (dogs.isEmpty()) {
                        Toast.makeText(this, R.string.no_dogs_for_request, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_dogs, Toast.LENGTH_SHORT).show();
                });
    }

    private void createWalkRequest() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (dogs.isEmpty() || dogSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(this, R.string.no_dogs_for_request, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedDateIso)) {
            dateEditText.setError(getString(R.string.error_date_required));
            return;
        }
        if (TextUtils.isEmpty(selectedTime)) {
            timeEditText.setError(getString(R.string.error_time_required));
            return;
        }

        int durationMinutes = durationValues.get(durationSpinner.getSelectedItemPosition());
        if (durationMinutes <= 0) {
            Toast.makeText(this, R.string.error_duration_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String maxPriceText = maxPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(maxPriceText)) {
            maxPriceEditText.setError(getString(R.string.error_price_required));
            return;
        }
        if (!ValidationUtils.isPositivePrice(maxPriceText)) {
            maxPriceEditText.setError(getString(R.string.error_price_positive));
            return;
        }
        if (pickupLat == null || pickupLng == null) {
            Toast.makeText(this, R.string.error_pickup_location_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double maxPrice = Double.parseDouble(maxPriceText);
        String pickupLocationLabel = ValidationUtils.normalizeSpaces(
                pickupLocationLabelEditText.getText().toString()
        );
        String notes = ValidationUtils.normalizeSpaces(notesEditText.getText().toString());
        Dog selectedDog = dogs.get(dogSpinner.getSelectedItemPosition());
        WalkRequest walkRequest = new WalkRequest(
                null,
                currentUser.getUid(),
                selectedDog.getId(),
                selectedDog.getName(),
                selectedDateIso,
                selectedTime,
                durationMinutes,
                maxPrice,
                notes,
                FirestoreConstants.WALK_REQUEST_STATUS_OPEN
        );
        walkRequest.setPickupLat(pickupLat);
        walkRequest.setPickupLng(pickupLng);
        walkRequest.setPickupLocationLabel(pickupLocationLabel);

        setLoading(true);
        walkRequestRepository.createWalkRequest(walkRequest)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.walk_request_created, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_create_walk_request, Toast.LENGTH_SHORT).show();
                });
    }

    private void requestPickupLocation() {
        if (permissionRequestInProgress || locationRequestInProgress) {
            return;
        }
        if (!LocationUtils.hasLocationPermission(this)) {
            permissionRequestInProgress = true;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_PICKUP_LOCATION_PERMISSION
            );
            return;
        }
        loadCurrentPickupLocation();
    }

    private void loadCurrentPickupLocation() {
        if (!LocationUtils.hasLocationPermission(this)) {
            Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationRequestInProgress) {
            return;
        }

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
                    .addOnSuccessListener(this, this::handlePickupLocation)
                    .addOnFailureListener(this, error -> {
                        finishLocationRequest();
                        Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException error) {
            finishLocationRequest();
            Toast.makeText(this, R.string.error_location_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePickupLocation(Location location) {
        finishLocationRequest();
        if (location == null) {
            Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        pickupLat = location.getLatitude();
        pickupLng = location.getLongitude();
        pickupLocationStatusTextView.setText(R.string.pickup_location_saved);
        Toast.makeText(this, R.string.pickup_location_saved, Toast.LENGTH_SHORT).show();
    }

    private void openManualPickupLocation() {
        if (!PlacesLocationHelper.isManualSearchAvailable(this)) {
            Toast.makeText(this, R.string.manual_location_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            startActivityForResult(
                    PlacesLocationHelper.buildAutocompleteIntent(this),
                    REQUEST_PICKUP_PLACE
            );
        } catch (RuntimeException error) {
            Toast.makeText(this, R.string.manual_location_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PICKUP_PLACE) {
            return;
        }

        if (resultCode == RESULT_OK && data != null) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            LatLng latLng = place.getLatLng();
            if (latLng == null) {
                Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
                return;
            }
            pickupLat = latLng.latitude;
            pickupLng = latLng.longitude;
            String label = PlacesLocationHelper.getReadableLabel(place);
            pickupLocationLabelEditText.setText(label);
            pickupLocationStatusTextView.setText(
                    TextUtils.isEmpty(label) ? getString(R.string.pickup_location_saved) : label
            );
            Toast.makeText(this, R.string.pickup_location_saved, Toast.LENGTH_SHORT).show();
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
            Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_PICKUP_LOCATION_PERMISSION) {
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

        if (granted) {
            loadCurrentPickupLocation();
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
        createButton.setEnabled(!loading && !dogs.isEmpty());
        currentLocationButton.setEnabled(!loading);
        manualLocationButton.setEnabled(!loading);
    }
}
