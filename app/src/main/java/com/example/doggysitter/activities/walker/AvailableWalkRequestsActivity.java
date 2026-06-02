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

public class AvailableWalkRequestsActivity extends AppCompatActivity implements WalkRequestAdapter.Listener {
    private static final String TAG = "AvailableWalkRequestsActivity";

    private RecyclerView walkRequestsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private WalkRequestAdapter walkRequestAdapter;
    private WalkRequestRepository walkRequestRepository;
    private WalkerProfileRepository walkerProfileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_walk_requests);

        walkRequestRepository = new WalkRequestRepository();
        walkerProfileRepository = new WalkerProfileRepository();
        walkRequestsRecyclerView = findViewById(R.id.recycler_walk_requests);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);

        walkRequestAdapter = new WalkRequestAdapter(this, true, false, false);
        walkRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        walkRequestsRecyclerView.setAdapter(walkRequestAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailableWalkRequests();
    }

    private void loadAvailableWalkRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkerProfileRepository.getProfile(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        setLoading(false);
                        walkRequestAdapter.submitList(new ArrayList<>());
                        showEmptyMessage(R.string.complete_walker_profile_required);
                        return;
                    }

                    Double serviceLat = getNumberField(documentSnapshot, FirestoreConstants.FIELD_SERVICE_LAT);
                    Double serviceLng = getNumberField(documentSnapshot, FirestoreConstants.FIELD_SERVICE_LNG);
                    Double serviceRadiusKm = getNumberField(
                            documentSnapshot,
                            FirestoreConstants.FIELD_SERVICE_RADIUS_KM
                    );

                    if (serviceLat == null || serviceLng == null
                            || serviceRadiusKm == null || serviceRadiusKm < 1) {
                        setLoading(false);
                        walkRequestAdapter.submitList(new ArrayList<>());
                        showEmptyMessage(R.string.service_area_required);
                        return;
                    }

                    loadOpenWalkRequests(serviceLat, serviceLng, serviceRadiusKm);
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            FirestoreErrorUtils.getReadErrorMessageResId(
                                    TAG,
                                    "Failed to load walker profile for available requests",
                                    error,
                                    R.string.error_load_walker_profile
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadOpenWalkRequests(double serviceLat, double serviceLng, double serviceRadiusKm) {
        walkRequestRepository.getOpenWalkRequests()
                .addOnSuccessListener(querySnapshot -> {
                    setLoading(false);
                    List<WalkRequest> walkRequests = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        WalkRequest walkRequest = documentSnapshot.toObject(WalkRequest.class);
                        if (walkRequest != null) {
                            if (walkRequest.getId() == null || walkRequest.getId().trim().isEmpty()) {
                                walkRequest.setId(documentSnapshot.getId());
                            }
                            if (walkRequest.getPickupLat() == null || walkRequest.getPickupLng() == null) {
                                continue;
                            }
                            double distanceKm = LocationUtils.calculateDistanceKm(
                                    serviceLat,
                                    serviceLng,
                                    walkRequest.getPickupLat(),
                                    walkRequest.getPickupLng()
                            );
                            if (distanceKm <= serviceRadiusKm) {
                                walkRequest.setDistanceKm(distanceKm);
                                walkRequests.add(walkRequest);
                            }
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
                                    "Failed to load open walk requests",
                                    error,
                                    R.string.error_load_walk_requests
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Override
    public void onAcceptWalkRequest(WalkRequest walkRequest) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkRequestRepository.acceptWalkRequest(walkRequest.getId(), currentUser.getUid())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.walk_request_accepted, Toast.LENGTH_SHORT).show();
                    loadAvailableWalkRequests();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, FirestoreErrorUtils.getTransitionWriteErrorMessage(
                            this,
                            TAG,
                            "Failed to accept walk request",
                            error,
                            R.string.error_accept_walk_request
                    ), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setText(R.string.no_available_walk_requests);
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        walkRequestsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showEmptyMessage(int messageResId) {
        emptyTextView.setText(messageResId);
        emptyTextView.setVisibility(View.VISIBLE);
        walkRequestsRecyclerView.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (walkRequestAdapter != null) {
            walkRequestAdapter.setActionsEnabled(!loading);
        }
    }

    private Double getNumberField(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
}
