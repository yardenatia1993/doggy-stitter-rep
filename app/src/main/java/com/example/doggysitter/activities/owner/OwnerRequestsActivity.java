package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.OwnerWalkRequestAdapter;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.WalkerProfileRepository;
import com.example.doggysitter.repositories.WalkRequestRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.FirestoreErrorUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OwnerRequestsActivity extends AppCompatActivity implements OwnerWalkRequestAdapter.Listener {
    private static final String TAG = "OwnerRequestsActivity";

    private RecyclerView ownerRequestsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private OwnerWalkRequestAdapter ownerWalkRequestAdapter;
    private WalkRequestRepository walkRequestRepository;
    private WalkerProfileRepository walkerProfileRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_requests);

        walkRequestRepository = new WalkRequestRepository();
        walkerProfileRepository = new WalkerProfileRepository();
        ownerRequestsRecyclerView = findViewById(R.id.recycler_owner_requests);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);

        ownerWalkRequestAdapter = new OwnerWalkRequestAdapter(this);
        ownerRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ownerRequestsRecyclerView.setAdapter(ownerWalkRequestAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerRequests();
    }

    private void loadOwnerRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkRequestRepository.getRequestsForOwner(currentUser.getUid())
                .addOnSuccessListener(querySnapshot -> {
                    List<WalkRequest> walkRequests = new ArrayList<>();
                    Set<String> walkerIds = new HashSet<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        WalkRequest walkRequest = documentSnapshot.toObject(WalkRequest.class);
                        if (walkRequest != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(walkRequest.getId()))) {
                                walkRequest.setId(documentSnapshot.getId());
                            }
                            String walkerId = ValidationUtils.normalizeSpaces(walkRequest.getWalkerId());
                            if (!TextUtils.isEmpty(walkerId)) {
                                walkerIds.add(walkerId);
                            }
                            walkRequests.add(walkRequest);
                        }
                    }

                    if (walkRequests.isEmpty()) {
                        setLoading(false);
                        ownerWalkRequestAdapter.submitList(new ArrayList<>(), new HashMap<>());
                        updateEmptyState(true);
                        return;
                    }

                    loadWalkerNames(walkRequests, walkerIds);
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            FirestoreErrorUtils.getReadErrorMessageResId(
                                    TAG,
                                    "Failed to load owner walk requests",
                                    error,
                                    R.string.error_load_walk_requests
                            ),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadWalkerNames(List<WalkRequest> walkRequests, Set<String> walkerIds) {
        if (walkerIds.isEmpty()) {
            setLoading(false);
            ownerWalkRequestAdapter.submitList(walkRequests, new HashMap<>());
            updateEmptyState(false);
            return;
        }

        List<String> orderedWalkerIds = new ArrayList<>(walkerIds);
        List<Task<DocumentSnapshot>> walkerTasks = new ArrayList<>();
        for (String walkerId : orderedWalkerIds) {
            walkerTasks.add(walkerProfileRepository.getProfile(walkerId));
        }

        Tasks.whenAllComplete(walkerTasks)
                .addOnCompleteListener(task -> {
                    Map<String, String> walkerNamesById = new HashMap<>();
                    for (int index = 0; index < walkerTasks.size(); index++) {
                        String walkerId = orderedWalkerIds.get(index);
                        String walkerName = getString(R.string.selected_walker_fallback);
                        Task<DocumentSnapshot> walkerTask = walkerTasks.get(index);
                        if (walkerTask.isSuccessful() && walkerTask.getResult() != null) {
                            String fullName = ValidationUtils.normalizeSpaces(
                                    walkerTask.getResult().getString(FirestoreConstants.FIELD_FULL_NAME)
                            );
                            if (!TextUtils.isEmpty(fullName)) {
                                walkerName = fullName;
                            }
                        } else {
                            FirestoreErrorUtils.log(
                                    TAG,
                                    "Failed to load walker name",
                                    walkerTask.getException()
                            );
                        }
                        walkerNamesById.put(walkerId, walkerName);
                    }

                    setLoading(false);
                    ownerWalkRequestAdapter.submitList(walkRequests, walkerNamesById);
                    updateEmptyState(false);
                });
    }

    @Override
    public void onCancelRequest(WalkRequest walkRequest) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_request_confirmation)
                .setPositiveButton(R.string.confirm, (dialog, which) -> cancelRequest(walkRequest))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onReviewWalker(WalkRequest walkRequest) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(ReviewActivity.EXTRA_REQUEST_ID, walkRequest.getId());
        startActivity(intent);
    }

    private void cancelRequest(WalkRequest walkRequest) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkRequestRepository.cancelRequest(walkRequest.getId(), currentUser.getUid())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.walk_request_canceled, Toast.LENGTH_SHORT).show();
                    loadOwnerRequests();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, FirestoreErrorUtils.getTransitionWriteErrorMessage(
                            this,
                            TAG,
                            "Failed to cancel walk request",
                            error,
                            R.string.error_cancel_walk_request
                    ), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        ownerRequestsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (ownerWalkRequestAdapter != null) {
            ownerWalkRequestAdapter.setActionsEnabled(!loading);
        }
    }
}
