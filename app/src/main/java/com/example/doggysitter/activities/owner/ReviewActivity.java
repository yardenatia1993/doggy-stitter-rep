package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.repositories.ReviewRepository;
import com.example.doggysitter.repositories.WalkRequestRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class ReviewActivity extends AppCompatActivity {
    public static final String EXTRA_REQUEST_ID = "requestId";

    private RatingBar ratingBar;
    private EditText commentEditText;
    private ProgressBar progressBar;
    private Button submitButton;
    private WalkRequestRepository walkRequestRepository;
    private ReviewRepository reviewRepository;
    private String requestId;
    private String ownerId;
    private boolean requestCanBeReviewed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        walkRequestRepository = new WalkRequestRepository();
        reviewRepository = new ReviewRepository();
        ratingBar = findViewById(R.id.rating_bar);
        commentEditText = findViewById(R.id.edit_comment);
        progressBar = findViewById(R.id.progress_bar);
        submitButton = findViewById(R.id.button_submit_review);

        requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        submitButton.setOnClickListener(view -> submitReview());

        loadRequest();
    }

    private void loadRequest() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ownerId = currentUser.getUid();

        if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(requestId))) {
            Toast.makeText(this, R.string.error_load_walk_request, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        walkRequestRepository.getRequest(requestId)
                .addOnSuccessListener(this::handleRequestLoaded)
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_load_walk_request, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void handleRequestLoaded(DocumentSnapshot documentSnapshot) {
        setLoading(false);
        if (!documentSnapshot.exists()) {
            Toast.makeText(this, R.string.error_load_walk_request, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String requestOwnerId = documentSnapshot.getString(FirestoreConstants.FIELD_OWNER_ID);
        String status = documentSnapshot.getString(FirestoreConstants.FIELD_STATUS);
        String walkerId = documentSnapshot.getString(FirestoreConstants.FIELD_WALKER_ID);

        if (!ownerId.equals(requestOwnerId)
                || !FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)
                || TextUtils.isEmpty(walkerId)) {
            Toast.makeText(this, R.string.error_review_completed_only, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Boolean.TRUE.equals(documentSnapshot.getBoolean(FirestoreConstants.FIELD_REVIEWED))) {
            Toast.makeText(this, R.string.error_review_already_sent, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestCanBeReviewed = true;
        submitButton.setEnabled(true);
    }

    private void submitReview() {
        if (!requestCanBeReviewed) {
            Toast.makeText(this, R.string.error_review_completed_only, Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = getSelectedRating();
        if (rating < 1 || rating > 5) {
            Toast.makeText(this, R.string.error_rating_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = ValidationUtils.normalizeSpaces(commentEditText.getText().toString());
        setLoading(true);
        reviewRepository.submitReview(requestId, ownerId, rating, comment)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.review_sent, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_submit_review, Toast.LENGTH_SHORT).show();
                });
    }

    private int getSelectedRating() {
        return Math.round(ratingBar.getRating());
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading && requestCanBeReviewed);
    }
}
