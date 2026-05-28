package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.AdminWalkRequestAdapter;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminWalkRequestsActivity extends AppCompatActivity {
    private final List<WalkRequest> allWalkRequests = new ArrayList<>();
    private AdminRepository adminRepository;
    private AdminWalkRequestAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private Spinner statusFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_walk_requests);

        adminRepository = new AdminRepository();
        recyclerView = findViewById(R.id.recycler_walk_requests);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);
        statusFilterSpinner = findViewById(R.id.spinner_status_filter);

        adapter = new AdminWalkRequestAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setupStatusFilter();
        loadWalkRequests();
    }

    private void setupStatusFilter() {
        List<String> filterLabels = new ArrayList<>();
        filterLabels.add(getString(R.string.filter_all));
        filterLabels.add(getString(R.string.filter_open_requests));
        filterLabels.add(getString(R.string.filter_accepted_requests));
        filterLabels.add(getString(R.string.filter_completed_requests));
        filterLabels.add(getString(R.string.filter_canceled_requests));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterLabels
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(spinnerAdapter);
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyStatusFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyStatusFilter();
            }
        });
    }

    private void loadWalkRequests() {
        setLoading(true);
        adminRepository.getAllWalkRequests()
                .addOnSuccessListener(querySnapshot -> {
                    allWalkRequests.clear();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        WalkRequest walkRequest = documentSnapshot.toObject(WalkRequest.class);
                        if (walkRequest != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(walkRequest.getId()))) {
                                walkRequest.setId(documentSnapshot.getId());
                            }
                            allWalkRequests.add(walkRequest);
                        }
                    }
                    setLoading(false);
                    applyStatusFilter();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_admin_data, Toast.LENGTH_SHORT).show();
                });
    }

    private void applyStatusFilter() {
        String selectedStatus = getSelectedStatus();
        List<WalkRequest> filteredRequests = new ArrayList<>();
        for (WalkRequest walkRequest : allWalkRequests) {
            if (selectedStatus == null || selectedStatus.equals(walkRequest.getStatus())) {
                filteredRequests.add(walkRequest);
            }
        }
        adapter.submitList(filteredRequests);
        updateEmptyState(filteredRequests.isEmpty());
    }

    private String getSelectedStatus() {
        int selectedPosition = statusFilterSpinner.getSelectedItemPosition();
        if (selectedPosition == 1) {
            return FirestoreConstants.WALK_REQUEST_STATUS_OPEN;
        }
        if (selectedPosition == 2) {
            return FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED;
        }
        if (selectedPosition == 3) {
            return FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED;
        }
        if (selectedPosition == 4) {
            return FirestoreConstants.WALK_REQUEST_STATUS_CANCELED;
        }
        return null;
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
