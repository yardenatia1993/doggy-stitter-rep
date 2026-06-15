package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.AdminReviewAdapter;
import com.example.doggysitter.models.Review;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doggysitter.utils.FirestoreConstants;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReviewsActivity extends AdminBaseActivity {
    private AdminRepository adminRepository;
    private AdminReviewAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private final Map<String, String> userNamesById = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        adminRepository = new AdminRepository();
        TextView titleTextView = findViewById(R.id.text_title);
        recyclerView = findViewById(R.id.recycler_items);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);

        titleTextView.setText(R.string.reviews);
        emptyTextView.setText(R.string.no_admin_reviews);
        adapter = new AdminReviewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        requireAdminAccess(this::loadReviews);
    }

    private void loadReviews() {
        setLoading(true);
        Task<QuerySnapshot> reviewsTask = adminRepository.getAllReviews();
        Task<QuerySnapshot> usersTask = adminRepository.getAllUsers();

        Tasks.whenAllComplete(reviewsTask, usersTask)
                .addOnCompleteListener(task -> {
                    if (!reviewsTask.isSuccessful()) {
                        setLoading(false);
                        handleAdminDataLoadFailure("Failed to load admin reviews", reviewsTask.getException());
                        return;
                    }
                    if (!usersTask.isSuccessful()) {
                        setLoading(false);
                        handleAdminDataLoadFailure(
                                "Failed to load user names for admin reviews",
                                usersTask.getException()
                        );
                        return;
                    }

                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : reviewsTask.getResult().getDocuments()) {
                        Review review = documentSnapshot.toObject(Review.class);
                        if (review != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(review.getId()))) {
                                review.setId(documentSnapshot.getId());
                            }
                            reviews.add(review);
                        }
                    }
                    userNamesById.clear();
                    userNamesById.putAll(buildUserNamesById(usersTask.getResult()));
                    setLoading(false);
                    adapter.submitList(reviews, userNamesById);
                    updateEmptyState(reviews.isEmpty());
                });
    }

    private Map<String, String> buildUserNamesById(QuerySnapshot usersSnapshot) {
        Map<String, String> namesById = new HashMap<>();
        for (DocumentSnapshot documentSnapshot : usersSnapshot.getDocuments()) {
            String uid = ValidationUtils.normalizeSpaces(
                    documentSnapshot.getString(FirestoreConstants.FIELD_UID)
            );
            if (TextUtils.isEmpty(uid)) {
                uid = documentSnapshot.getId();
            }

            String fullName = ValidationUtils.normalizeSpaces(
                    documentSnapshot.getString(FirestoreConstants.FIELD_FULL_NAME)
            );
            if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(fullName)) {
                namesById.put(uid, fullName);
            }
        }
        return namesById;
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
