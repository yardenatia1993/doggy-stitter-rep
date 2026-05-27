package com.example.doggysitter;

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
    private RecyclerView jobsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private WalkRequestAdapter walkRequestAdapter;
    private WalkRequestRepository walkRequestRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_walker_jobs);

        walkRequestRepository = new WalkRequestRepository();
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
        loadJobs();
    }

    private void loadJobs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkRequestRepository.getAcceptedWalkRequestsForWalker(currentUser.getUid())
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
                            walkRequests.add(walkRequest);
                        }
                    }

                    walkRequestAdapter.submitList(walkRequests);
                    updateEmptyState(walkRequests.isEmpty());
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_walk_requests, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        jobsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
