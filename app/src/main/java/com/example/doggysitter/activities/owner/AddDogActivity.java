package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Dog;
import com.example.doggysitter.repositories.DogRepository;
import com.example.doggysitter.utils.FirestoreErrorUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddDogActivity extends AppCompatActivity {
    public static final String EXTRA_DOG_ID = "dogId";
    private static final String TAG = "AddDogActivity";

    private TextView titleTextView;
    private EditText dogNameEditText;
    private Spinner yearsSpinner;
    private Spinner monthsSpinner;
    private AutoCompleteTextView breedAutoCompleteTextView;
    private TextView customBreedLabelTextView;
    private EditText customBreedEditText;
    private EditText notesEditText;
    private Button saveButton;
    private ProgressBar progressBar;
    private DogRepository dogRepository;
    private List<String> breeds;
    private String breedOther;
    private String editingDogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dog);

        dogRepository = new DogRepository();
        breedOther = getString(R.string.breed_other);
        breeds = Arrays.asList(getResources().getStringArray(R.array.dog_breeds));
        editingDogId = getIntent().getStringExtra(EXTRA_DOG_ID);

        initViews();
        setupAgeSpinners();
        setupBreedInput();

        if (!TextUtils.isEmpty(editingDogId)) {
            titleTextView.setText(R.string.title_edit_dog);
            loadDogForEdit(editingDogId);
        }

        saveButton.setOnClickListener(view -> saveDog());
    }

    private void initViews() {
        titleTextView = findViewById(R.id.text_title);
        dogNameEditText = findViewById(R.id.edit_dog_name);
        yearsSpinner = findViewById(R.id.spinner_years);
        monthsSpinner = findViewById(R.id.spinner_months);
        breedAutoCompleteTextView = findViewById(R.id.edit_breed);
        customBreedLabelTextView = findViewById(R.id.text_custom_breed_label);
        customBreedEditText = findViewById(R.id.edit_custom_breed);
        notesEditText = findViewById(R.id.edit_notes);
        saveButton = findViewById(R.id.button_save_dog);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupAgeSpinners() {
        List<String> years = new ArrayList<>();
        for (int year = 0; year <= 20; year++) {
            years.add(year + " " + getString(R.string.years));
        }

        List<String> months = new ArrayList<>();
        for (int month = 0; month <= 11; month++) {
            months.add(month + " " + getString(R.string.months));
        }

        yearsSpinner.setAdapter(createSimpleAdapter(years));
        monthsSpinner.setAdapter(createSimpleAdapter(months));
    }

    private void setupBreedInput() {
        ArrayAdapter<String> adapter = createSimpleAdapter(breeds);
        breedAutoCompleteTextView.setAdapter(adapter);
        breedAutoCompleteTextView.setThreshold(0);
        breedAutoCompleteTextView.setOnClickListener(view -> breedAutoCompleteTextView.showDropDown());
        breedAutoCompleteTextView.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                breedAutoCompleteTextView.showDropDown();
            }
        });
        breedAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence value, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence value, int start, int before, int count) {
                boolean isOther = breedOther.equals(ValidationUtils.normalizeSpaces(value.toString()));
                customBreedLabelTextView.setVisibility(isOther ? View.VISIBLE : View.GONE);
                customBreedEditText.setVisibility(isOther ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private ArrayAdapter<String> createSimpleAdapter(List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void loadDogForEdit(String dogId) {
        setLoading(true);
        dogRepository.getDog(dogId)
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    Dog dog = documentSnapshot.toObject(Dog.class);
                    if (dog == null) {
                        Toast.makeText(this, R.string.error_load_dog, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    dog.setId(documentSnapshot.getId());
                    populateDog(dog);
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            FirestoreErrorUtils.getReadErrorMessageResId(
                                    TAG,
                                    "Failed to load dog for editing",
                                    error,
                                    R.string.error_load_dog
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                });
    }

    private void populateDog(Dog dog) {
        dogNameEditText.setText(dog.getName());
        notesEditText.setText(dog.getNotes());
        setAgeSelection(getDogAgeMonths(dog));

        String breed = ValidationUtils.normalizeSpaces(dog.getBreed());
        if (breeds.contains(breed)) {
            breedAutoCompleteTextView.setText(breed, false);
        } else if (!TextUtils.isEmpty(breed)) {
            breedAutoCompleteTextView.setText(breedOther, false);
            customBreedLabelTextView.setVisibility(View.VISIBLE);
            customBreedEditText.setVisibility(View.VISIBLE);
            customBreedEditText.setText(breed);
        }
    }

    private int getDogAgeMonths(Dog dog) {
        Integer ageMonths = dog.getAgeMonths();
        if (ageMonths != null && ageMonths > 0) {
            return ageMonths;
        }

        Integer legacyAge = dog.getAge();
        if (legacyAge != null && legacyAge > 0) {
            return legacyAge * 12;
        }

        return 0;
    }

    private void setAgeSelection(int totalMonths) {
        int clampedMonths = Math.max(0, Math.min(totalMonths, 240));
        yearsSpinner.setSelection(clampedMonths / 12);
        monthsSpinner.setSelection(clampedMonths % 12);
    }

    private void saveDog() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = ValidationUtils.normalizeSpaces(dogNameEditText.getText().toString());
        String breed = getValidatedBreed();
        String notes = ValidationUtils.normalizeSpaces(notesEditText.getText().toString());
        int ageMonths = getSelectedAgeMonths();

        if (TextUtils.isEmpty(name)) {
            dogNameEditText.setError(getString(R.string.error_dog_name_required));
            return;
        }
        if (!ValidationUtils.isValidSingleLanguageName(name)) {
            dogNameEditText.setError(getString(R.string.error_invalid_dog_name));
            return;
        }
        if (ageMonths < 3) {
            Toast.makeText(this, R.string.error_age_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ageMonths > 240) {
            Toast.makeText(this, R.string.error_age_range, Toast.LENGTH_SHORT).show();
            return;
        }
        if (breed == null) {
            return;
        }

        setLoading(true);
        if (TextUtils.isEmpty(editingDogId)) {
            Dog dog = new Dog(null, currentUser.getUid(), name, ageMonths, breed, notes);
            dogRepository.addDog(dog)
                    .addOnSuccessListener(unused -> {
                        setLoading(false);
                        Toast.makeText(this, R.string.dog_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(error -> {
                        setLoading(false);
                        Toast.makeText(
                                this,
                                FirestoreErrorUtils.getWriteErrorMessageResId(
                                        TAG,
                                        "Failed to add dog",
                                        error,
                                        R.string.error_save_dog
                                ),
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        } else {
            dogRepository.updateDog(editingDogId, name, ageMonths, breed, notes)
                    .addOnSuccessListener(unused -> {
                        setLoading(false);
                        Toast.makeText(this, R.string.dog_updated, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(error -> {
                        setLoading(false);
                        Toast.makeText(
                                this,
                                FirestoreErrorUtils.getWriteErrorMessageResId(
                                        TAG,
                                        "Failed to update dog",
                                        error,
                                        R.string.error_update_dog
                                ),
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }
    }

    private String getValidatedBreed() {
        String selectedBreed = ValidationUtils.normalizeSpaces(breedAutoCompleteTextView.getText().toString());
        if (TextUtils.isEmpty(selectedBreed)) {
            breedAutoCompleteTextView.setError(getString(R.string.error_breed_required));
            return null;
        }
        if (!breeds.contains(selectedBreed)) {
            breedAutoCompleteTextView.setError(getString(R.string.error_breed_from_list));
            return null;
        }
        if (!breedOther.equals(selectedBreed)) {
            return selectedBreed;
        }

        String customBreed = ValidationUtils.normalizeSpaces(customBreedEditText.getText().toString());
        if (TextUtils.isEmpty(customBreed)) {
            customBreedEditText.setError(getString(R.string.error_custom_breed_required));
            return null;
        }
        if (!ValidationUtils.isValidSingleLanguageName(customBreed)) {
            customBreedEditText.setError(getString(R.string.error_invalid_custom_breed));
            return null;
        }
        return customBreed;
    }

    private int getSelectedAgeMonths() {
        return yearsSpinner.getSelectedItemPosition() * 12 + monthsSpinner.getSelectedItemPosition();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }
}
