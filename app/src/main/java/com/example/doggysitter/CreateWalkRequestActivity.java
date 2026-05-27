package com.example.doggysitter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateWalkRequestActivity extends AppCompatActivity {
    private Spinner dogSpinner;
    private EditText dateEditText;
    private EditText timeEditText;
    private Spinner durationSpinner;
    private EditText maxPriceEditText;
    private EditText notesEditText;
    private Button createButton;
    private ProgressBar progressBar;
    private DogRepository dogRepository;
    private WalkRequestRepository walkRequestRepository;
    private final List<Dog> dogs = new ArrayList<>();
    private final List<Integer> durationValues = new ArrayList<>();
    private String selectedDateIso;
    private String selectedTime;

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
        createButton.setOnClickListener(view -> createWalkRequest());
        loadDogs();
    }

    private void initViews() {
        dogSpinner = findViewById(R.id.spinner_dogs);
        dateEditText = findViewById(R.id.edit_date);
        timeEditText = findViewById(R.id.edit_time);
        durationSpinner = findViewById(R.id.spinner_duration);
        maxPriceEditText = findViewById(R.id.edit_max_price);
        notesEditText = findViewById(R.id.edit_notes);
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

        double maxPrice = Double.parseDouble(maxPriceText);
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

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        createButton.setEnabled(!loading && !dogs.isEmpty());
    }
}
