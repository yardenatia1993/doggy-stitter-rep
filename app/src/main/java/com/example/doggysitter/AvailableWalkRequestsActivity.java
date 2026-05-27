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

public class AvailableWalkRequestsActivity extends AppCompatActivity implements WalkRequestAdapter.Listener {
    private RecyclerView walkRequestsRecyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private WalkRequestAdapter walkRequestAdapter;
    private WalkRequestRepository walkRequestRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_walk_requests);

        walkRequestRepository = new WalkRequestRepository();
        walkRequestsRecyclerView = findViewById(R.id.recycler_walk_requests);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);

        walkRequestAdapter = new WalkRequestAdapter(this, true, false);
        walkRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        walkRequestsRecyclerView.setAdapter(walkRequestAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOpenWalkRequests();
    }

    private void loadOpenWalkRequests() {
        setLoading(true);
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
                    loadOpenWalkRequests();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_accept_walk_request, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        walkRequestsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
