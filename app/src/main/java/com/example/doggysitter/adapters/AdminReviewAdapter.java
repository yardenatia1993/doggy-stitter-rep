package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Review;
import com.example.doggysitter.utils.AdminDisplayUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.AdminReviewViewHolder> {
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, String> userNamesById = new HashMap<>();

    public void submitList(List<Review> newReviews, Map<String, String> newUserNamesById) {
        reviews.clear();
        reviews.addAll(newReviews);
        userNamesById.clear();
        if (newUserNamesById != null) {
            userNamesById.putAll(newUserNamesById);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_review, parent, false);
        return new AdminReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.ratingTextView.setText("דירוג: "
                + AdminDisplayUtils.formatStars(review.getRating()) + " (" + review.getRating() + ")");

        String comment = ValidationUtils.normalizeSpaces(review.getComment());
        holder.commentTextView.setVisibility(TextUtils.isEmpty(comment) ? View.GONE : View.VISIBLE);
        holder.commentTextView.setText("הערה: " + comment);

        holder.ownerIdTextView.setText("בעלים: "
                + AdminDisplayUtils.displayUserName(review.getOwnerId(), userNamesById));
        holder.walkerIdTextView.setText("דוגווקר: "
                + AdminDisplayUtils.displayUserName(review.getWalkerId(), userNamesById));
        holder.requestIdTextView.setText("בקשה: " + AdminDisplayUtils.fallbackText(review.getRequestId()));
        holder.createdAtTextView.setText("תאריך יצירה: "
                + AdminDisplayUtils.formatCreatedAt(review.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class AdminReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView ratingTextView;
        private final TextView commentTextView;
        private final TextView ownerIdTextView;
        private final TextView walkerIdTextView;
        private final TextView requestIdTextView;
        private final TextView createdAtTextView;

        AdminReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ratingTextView = itemView.findViewById(R.id.text_rating);
            commentTextView = itemView.findViewById(R.id.text_comment);
            ownerIdTextView = itemView.findViewById(R.id.text_owner_id);
            walkerIdTextView = itemView.findViewById(R.id.text_walker_id);
            requestIdTextView = itemView.findViewById(R.id.text_request_id);
            createdAtTextView = itemView.findViewById(R.id.text_created_at);
        }
    }
}
