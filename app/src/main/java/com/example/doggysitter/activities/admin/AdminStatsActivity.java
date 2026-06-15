package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Review;
import com.example.doggysitter.models.User;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.FirestoreConstants;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class AdminStatsActivity extends AdminBaseActivity {
    private AdminRepository adminRepository;
    private ProgressBar progressBar;
    private TextView usersCountTextView;
    private TextView ownersCountTextView;
    private TextView walkersCountTextView;
    private TextView adminsCountTextView;
    private TextView dogsCountTextView;
    private TextView walkRequestsCountTextView;
    private TextView openRequestsCountTextView;
    private TextView acceptedRequestsCountTextView;
    private TextView completedRequestsCountTextView;
    private TextView canceledRequestsCountTextView;
    private TextView reviewsCountTextView;
    private TextView averageRatingTextView;
    private View openRequestsBarView;
    private View openRequestsRemainderView;
    private View acceptedRequestsBarView;
    private View acceptedRequestsRemainderView;
    private View completedRequestsBarView;
    private View completedRequestsRemainderView;
    private View canceledRequestsBarView;
    private View canceledRequestsRemainderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stats);

        adminRepository = new AdminRepository();
        progressBar = findViewById(R.id.progress_bar);
        usersCountTextView = findViewById(R.id.text_users_count);
        ownersCountTextView = findViewById(R.id.text_owners_count);
        walkersCountTextView = findViewById(R.id.text_walkers_count);
        adminsCountTextView = findViewById(R.id.text_admins_count);
        dogsCountTextView = findViewById(R.id.text_dogs_count);
        walkRequestsCountTextView = findViewById(R.id.text_walk_requests_count);
        openRequestsCountTextView = findViewById(R.id.text_open_requests_count);
        acceptedRequestsCountTextView = findViewById(R.id.text_accepted_requests_count);
        completedRequestsCountTextView = findViewById(R.id.text_completed_requests_count);
        canceledRequestsCountTextView = findViewById(R.id.text_canceled_requests_count);
        reviewsCountTextView = findViewById(R.id.text_reviews_count);
        averageRatingTextView = findViewById(R.id.text_average_rating);
        openRequestsBarView = findViewById(R.id.bar_open_requests);
        openRequestsRemainderView = findViewById(R.id.bar_open_requests_remainder);
        acceptedRequestsBarView = findViewById(R.id.bar_accepted_requests);
        acceptedRequestsRemainderView = findViewById(R.id.bar_accepted_requests_remainder);
        completedRequestsBarView = findViewById(R.id.bar_completed_requests);
        completedRequestsRemainderView = findViewById(R.id.bar_completed_requests_remainder);
        canceledRequestsBarView = findViewById(R.id.bar_canceled_requests);
        canceledRequestsRemainderView = findViewById(R.id.bar_canceled_requests_remainder);

        requireAdminAccess(this::loadStats);
    }

    private void loadStats() {
        setLoading(true);
        Task<QuerySnapshot> usersTask = adminRepository.getAllUsers();
        Task<QuerySnapshot> dogsTask = adminRepository.getAllDogs();
        Task<QuerySnapshot> walkRequestsTask = adminRepository.getAllWalkRequests();
        Task<QuerySnapshot> reviewsTask = adminRepository.getAllReviews();

        Tasks.whenAllComplete(usersTask, dogsTask, walkRequestsTask, reviewsTask)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!usersTask.isSuccessful()
                            || !dogsTask.isSuccessful()
                            || !walkRequestsTask.isSuccessful()
                            || !reviewsTask.isSuccessful()) {
                        handleFailedStatsTasks(usersTask, dogsTask, walkRequestsTask, reviewsTask);
                        return;
                    }
                    bindStats(
                            usersTask.getResult(),
                            dogsTask.getResult(),
                            walkRequestsTask.getResult(),
                            reviewsTask.getResult()
                    );
                });
    }

    @SafeVarargs
    private final void handleFailedStatsTasks(Task<QuerySnapshot>... tasks) {
        for (Task<QuerySnapshot> task : tasks) {
            if (!task.isSuccessful()) {
                handleAdminDataLoadFailure("Failed to load admin statistics", task.getException());
                return;
            }
        }
    }

    private void bindStats(QuerySnapshot usersSnapshot, QuerySnapshot dogsSnapshot,
                           QuerySnapshot walkRequestsSnapshot, QuerySnapshot reviewsSnapshot) {
        int ownerCount = 0;
        int walkerCount = 0;
        int adminCount = 0;
        for (DocumentSnapshot documentSnapshot : usersSnapshot.getDocuments()) {
            User user = documentSnapshot.toObject(User.class);
            if (user == null) {
                continue;
            }
            if (FirestoreConstants.ROLE_OWNER.equals(user.getRole())) {
                ownerCount++;
            } else if (FirestoreConstants.ROLE_WALKER.equals(user.getRole())) {
                walkerCount++;
            } else if (FirestoreConstants.ROLE_ADMIN.equals(user.getRole())) {
                adminCount++;
            }
        }

        int openRequests = 0;
        int acceptedRequests = 0;
        int completedRequests = 0;
        int canceledRequests = 0;
        for (DocumentSnapshot documentSnapshot : walkRequestsSnapshot.getDocuments()) {
            WalkRequest walkRequest = documentSnapshot.toObject(WalkRequest.class);
            if (walkRequest == null) {
                continue;
            }
            String status = walkRequest.getStatus();
            if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
                openRequests++;
            } else if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
                acceptedRequests++;
            } else if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
                completedRequests++;
            } else if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
                canceledRequests++;
            }
        }

        int ratingSum = 0;
        int ratingCount = 0;
        for (DocumentSnapshot documentSnapshot : reviewsSnapshot.getDocuments()) {
            Review review = documentSnapshot.toObject(Review.class);
            if (review != null && review.getRating() > 0) {
                ratingSum += review.getRating();
                ratingCount++;
            }
        }

        int requestCount = walkRequestsSnapshot.size();
        usersCountTextView.setText(String.valueOf(usersSnapshot.size()));
        ownersCountTextView.setText(String.valueOf(ownerCount));
        walkersCountTextView.setText(String.valueOf(walkerCount));
        adminsCountTextView.setText(String.valueOf(adminCount));
        dogsCountTextView.setText(String.valueOf(dogsSnapshot.size()));
        walkRequestsCountTextView.setText(String.valueOf(requestCount));
        openRequestsCountTextView.setText(String.valueOf(openRequests));
        acceptedRequestsCountTextView.setText(String.valueOf(acceptedRequests));
        completedRequestsCountTextView.setText(String.valueOf(completedRequests));
        canceledRequestsCountTextView.setText(String.valueOf(canceledRequests));
        reviewsCountTextView.setText(String.valueOf(reviewsSnapshot.size()));
        averageRatingTextView.setText(formatAverageRating(ratingSum, ratingCount));
        updateStatusBars(openRequests, acceptedRequests, completedRequests, canceledRequests);
    }

    private void updateStatusBars(int openRequests, int acceptedRequests, int completedRequests,
                                  int canceledRequests) {
        int total = openRequests + acceptedRequests + completedRequests + canceledRequests;
        setBarWeights(openRequestsBarView, openRequestsRemainderView, openRequests, total);
        setBarWeights(acceptedRequestsBarView, acceptedRequestsRemainderView, acceptedRequests, total);
        setBarWeights(completedRequestsBarView, completedRequestsRemainderView, completedRequests, total);
        setBarWeights(canceledRequestsBarView, canceledRequestsRemainderView, canceledRequests, total);
    }

    private void setBarWeights(View barView, View remainderView, int count, int total) {
        float barWeight = total <= 0 ? 0f : count;
        float remainderWeight = total <= 0 ? 1f : Math.max(0, total - count);
        barView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                barWeight
        ));
        remainderView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                remainderWeight
        ));
    }

    private String formatAverageRating(int ratingSum, int ratingCount) {
        if (ratingCount <= 0) {
            return "אין דירוגים עדיין";
        }
        return String.format(Locale.getDefault(), "%.1f מתוך 5", (double) ratingSum / ratingCount);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
