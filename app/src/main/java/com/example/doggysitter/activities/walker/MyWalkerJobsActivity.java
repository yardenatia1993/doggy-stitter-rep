package com.example.doggysitter.activities.walker;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.WalkRequestAdapter;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.WalkRequestRepository;
import com.example.doggysitter.repositories.WalkerProfileRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.FirestoreErrorUtils;
import com.example.doggysitter.utils.LocationUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyWalkerJobsActivity extends AppCompatActivity {
    private static final String TAG = "MyWalkerJobsActivity";

    private RecyclerView jobsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private WalkRequestAdapter walkRequestAdapter;
    private WalkRequestRepository walkRequestRepository;
    private WalkerProfileRepository walkerProfileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_walker_jobs);

        walkRequestRepository = new WalkRequestRepository();
        walkerProfileRepository = new WalkerProfileRepository();
        jobsRecyclerView = findViewById(R.id.recycler_walk_requests);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);

        walkRequestAdapter = new WalkRequestAdapter(null, false, true);
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsRecyclerView.setAdapter(walkRequestAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileAndJobs();
    }

    private void loadProfileAndJobs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkerProfileRepository.getProfile(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    Double serviceLat = getNumberField(documentSnapshot, FirestoreConstants.FIELD_SERVICE_LAT);
                    Double serviceLng = getNumberField(documentSnapshot, FirestoreConstants.FIELD_SERVICE_LNG);
                    loadJobs(currentUser.getUid(), serviceLat, serviceLng);
                })
                .addOnFailureListener(error -> {
                    if (FirestoreErrorUtils.isPermissionDenied(error)) {
                        setLoading(false);
                        Toast.makeText(
                                this,
                                FirestoreErrorUtils.getReadErrorMessageResId(
                                        TAG,
                                        "Permission denied while loading walker profile for jobs",
                                        error,
                                        R.string.error_load_walker_profile
                                ),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    FirestoreErrorUtils.log(TAG, "Failed to load walker profile for jobs", error);
                    loadJobs(currentUser.getUid(), null, null);
                });
    }

    private void loadJobs(String walkerId, Double serviceLat, Double serviceLng) {
        walkRequestRepository.getAcceptedWalkRequestsForWalker(walkerId)
                .addOnSuccessListener(querySnapshot -> {
                    setLoading(false);
                    List<WalkRequest> walkRequests = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        WalkRequest walkRequest = documentSnapshot.toObject(WalkRequest.class);
                        if (walkRequest != null
                                && FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(walkRequest.getStatus())) {
                            if (walkRequest.getId() == null || walkRequest.getId().trim().isEmpty()) {
                                walkRequest.setId(documentSnapshot.getId());
                            }
                            if (serviceLat != null && serviceLng != null
                                    && walkRequest.getPickupLat() != null
                                    && walkRequest.getPickupLng() != null) {
                                walkRequest.setDistanceKm(LocationUtils.calculateDistanceKm(
                                        serviceLat,
                                        serviceLng,
                                        walkRequest.getPickupLat(),
                                        walkRequest.getPickupLng()
                                ));
                            }
                            walkRequests.add(walkRequest);
                        }
                    }

                    walkRequestAdapter.submitList(walkRequests);
                    updateEmptyState(walkRequests.isEmpty());
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            FirestoreErrorUtils.getReadErrorMessageResId(
                                    TAG,
                                    "Failed to load walker jobs",
                                    error,
                                    R.string.error_load_walk_requests
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        jobsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private Double getNumberField(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
}
