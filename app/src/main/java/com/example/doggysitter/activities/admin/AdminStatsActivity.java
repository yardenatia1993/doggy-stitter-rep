package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Review;
import com.example.doggysitter.models.User;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.FirestoreConstants;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView openRequestsCountTextView;
    private TextView acceptedRequestsCountTextView;
    private TextView completedRequestsCountTextView;
    private TextView canceledRequestsCountTextView;
    private TextView reviewsCountTextView;
    private TextView averageRatingTextView;

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
        openRequestsCountTextView = findViewById(R.id.text_open_requests_count);
        acceptedRequestsCountTextView = findViewById(R.id.text_accepted_requests_count);
        completedRequestsCountTextView = findViewById(R.id.text_completed_requests_count);
        canceledRequestsCountTextView = findViewById(R.id.text_canceled_requests_count);
        reviewsCountTextView = findViewById(R.id.text_reviews_count);
        averageRatingTextView = findViewById(R.id.text_average_rating);

        requireAdminAccess(this::loadStats);
    }

    private void loadStats() {
        setLoading(true);
        Task<QuerySnapshot> usersTask = adminRepository.getAllUsers();
        Task<QuerySnapshot> walkRequestsTask = adminRepository.getAllWalkRequests();
        Task<QuerySnapshot> reviewsTask = adminRepository.getAllReviews();

        Tasks.whenAllComplete(usersTask, walkRequestsTask, reviewsTask)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!usersTask.isSuccessful()
                            || !walkRequestsTask.isSuccessful()
                            || !reviewsTask.isSuccessful()) {
                        handleFailedStatsTasks(usersTask, walkRequestsTask, reviewsTask);
                        return;
                    }
                    bindStats(usersTask.getResult(), walkRequestsTask.getResult(), reviewsTask.getResult());
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

    private void bindStats(QuerySnapshot usersSnapshot, QuerySnapshot walkRequestsSnapshot,
                           QuerySnapshot reviewsSnapshot) {
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

        usersCountTextView.setText("מספר משתמשים: " + usersSnapshot.size());
        ownersCountTextView.setText("מספר בעלי כלבים: " + ownerCount);
        walkersCountTextView.setText("מספר דוגווקרים: " + walkerCount);
        adminsCountTextView.setText("מספר מנהלים: " + adminCount);
        openRequestsCountTextView.setText("מספר בקשות פתוחות: " + openRequests);
        acceptedRequestsCountTextView.setText("מספר בקשות שהתקבלו: " + acceptedRequests);
        completedRequestsCountTextView.setText("מספר בקשות שהושלמו: " + completedRequests);
        canceledRequestsCountTextView.setText("מספר בקשות שבוטלו: " + canceledRequests);
        reviewsCountTextView.setText("מספר ביקורות: " + reviewsSnapshot.size());
        averageRatingTextView.setText("דירוג ממוצע כללי של דוגווקרים: "
                + formatAverageRating(ratingSum, ratingCount));
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
