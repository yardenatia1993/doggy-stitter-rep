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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminReviewsActivity extends AppCompatActivity {
    private AdminRepository adminRepository;
    private AdminReviewAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;

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
        loadReviews();
    }

    private void loadReviews() {
        setLoading(true);
        adminRepository.getAllReviews()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        Review review = documentSnapshot.toObject(Review.class);
                        if (review != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(review.getId()))) {
                                review.setId(documentSnapshot.getId());
                            }
                            reviews.add(review);
                        }
                    }
                    setLoading(false);
                    adapter.submitList(reviews);
                    updateEmptyState(reviews.isEmpty());
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_admin_data, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
