package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.utils.AdminDisplayUtils;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.PriceUtils;
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

public class AdminWalkRequestAdapter
        extends RecyclerView.Adapter<AdminWalkRequestAdapter.AdminWalkRequestViewHolder> {
    private final List<WalkRequest> walkRequests = new ArrayList<>();
    private final Map<String, String> userNamesById = new HashMap<>();

    public void submitList(List<WalkRequest> newWalkRequests, Map<String, String> newUserNamesById) {
        walkRequests.clear();
        walkRequests.addAll(newWalkRequests);
        userNamesById.clear();
        if (newUserNamesById != null) {
            userNamesById.putAll(newUserNamesById);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminWalkRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_walk_request, parent, false);
        return new AdminWalkRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminWalkRequestViewHolder holder, int position) {
        WalkRequest walkRequest = walkRequests.get(position);
        holder.dogNameTextView.setText("שם הכלב: " + AdminDisplayUtils.fallbackText(walkRequest.getDogName()));
        holder.ownerIdTextView.setText("בעלים: "
                + AdminDisplayUtils.displayUserName(walkRequest.getOwnerId(), userNamesById));

        String walkerId = ValidationUtils.normalizeSpaces(walkRequest.getWalkerId());
        holder.walkerIdTextView.setVisibility(TextUtils.isEmpty(walkerId) ? View.GONE : View.VISIBLE);
        holder.walkerIdTextView.setText("דוגווקר: "
                + AdminDisplayUtils.displayUserName(walkerId, userNamesById));

        holder.dateTextView.setText("תאריך: " + DateTimeUtils.formatDisplayDate(walkRequest.getDate()));
        holder.timeTextView.setText("שעה: " + AdminDisplayUtils.fallbackText(walkRequest.getTime()));
        holder.durationTextView.setText("משך הטיול: " + walkRequest.getDurationMinutes() + " דקות");
        holder.priceTextView.setText("מחיר מקסימלי לכל הטיול: "
                + PriceUtils.formatWholeShekelAmount(walkRequest.getMaxPrice()) + " ₪");

        String locationLabel = ValidationUtils.normalizeSpaces(walkRequest.getPickupLocationLabel());
        holder.locationTextView.setVisibility(TextUtils.isEmpty(locationLabel) ? View.GONE : View.VISIBLE);
        holder.locationTextView.setText("מיקום / יישוב: " + locationLabel);

        holder.statusTextView.setText("סטטוס: "
                + AdminDisplayUtils.formatWalkRequestStatus(walkRequest.getStatus()));
        holder.reviewedTextView.setText("האם דורג: " + AdminDisplayUtils.formatReviewed(walkRequest.getReviewed()));
    }

    @Override
    public int getItemCount() {
        return walkRequests.size();
    }

    static class AdminWalkRequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView dogNameTextView;
        private final TextView ownerIdTextView;
        private final TextView walkerIdTextView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        private final TextView durationTextView;
        private final TextView priceTextView;
        private final TextView locationTextView;
        private final TextView statusTextView;
        private final TextView reviewedTextView;

        AdminWalkRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            dogNameTextView = itemView.findViewById(R.id.text_dog_name);
            ownerIdTextView = itemView.findViewById(R.id.text_owner_id);
            walkerIdTextView = itemView.findViewById(R.id.text_walker_id);
            dateTextView = itemView.findViewById(R.id.text_date);
            timeTextView = itemView.findViewById(R.id.text_time);
            durationTextView = itemView.findViewById(R.id.text_duration);
            priceTextView = itemView.findViewById(R.id.text_price);
            locationTextView = itemView.findViewById(R.id.text_location);
            statusTextView = itemView.findViewById(R.id.text_status);
            reviewedTextView = itemView.findViewById(R.id.text_reviewed);
        }
    }
}
