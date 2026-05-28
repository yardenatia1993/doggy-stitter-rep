package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.PriceUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OwnerWalkRequestAdapter
        extends RecyclerView.Adapter<OwnerWalkRequestAdapter.OwnerWalkRequestViewHolder> {
    public interface Listener {
        void onCancelRequest(WalkRequest walkRequest);

        void onCompleteRequest(WalkRequest walkRequest);

        void onReviewWalker(WalkRequest walkRequest);
    }

    private final List<WalkRequest> walkRequests = new ArrayList<>();
    private final Map<String, String> walkerNamesById = new HashMap<>();
    private final Listener listener;
    private boolean actionsEnabled = true;

    public OwnerWalkRequestAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<WalkRequest> newWalkRequests, Map<String, String> newWalkerNamesById) {
        walkRequests.clear();
        walkRequests.addAll(newWalkRequests);
        walkerNamesById.clear();
        walkerNamesById.putAll(newWalkerNamesById);
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean actionsEnabled) {
        if (this.actionsEnabled == actionsEnabled) {
            return;
        }
        this.actionsEnabled = actionsEnabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnerWalkRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_owner_walk_request, parent, false);
        return new OwnerWalkRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerWalkRequestViewHolder holder, int position) {
        WalkRequest walkRequest = walkRequests.get(position);
        Context context = holder.itemView.getContext();

        holder.dogNameTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_dog_name),
                walkRequest.getDogName()
        ));
        holder.dateTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_date),
                DateTimeUtils.formatDisplayDate(walkRequest.getDate())
        ));
        holder.timeTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_time),
                walkRequest.getTime()
        ));
        holder.durationTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_duration),
                walkRequest.getDurationMinutes()
        ));
        holder.maxPriceTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_max_price),
                PriceUtils.formatWholeShekelAmount(walkRequest.getMaxPrice())
        ));

        String pickupLocationLabel = ValidationUtils.normalizeSpaces(walkRequest.getPickupLocationLabel());
        holder.pickupLocationTextView.setVisibility(
                TextUtils.isEmpty(pickupLocationLabel) ? View.GONE : View.VISIBLE
        );
        holder.pickupLocationTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_location_label),
                pickupLocationLabel
        ));

        holder.statusTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.status_label),
                getDisplayStatus(context, walkRequest.getStatus())
        ));

        bindWalkerName(holder, context, walkRequest);
        bindActionButton(holder, context, walkRequest);
    }

    @Override
    public int getItemCount() {
        return walkRequests.size();
    }

    private void bindWalkerName(OwnerWalkRequestViewHolder holder, Context context, WalkRequest walkRequest) {
        String walkerId = ValidationUtils.normalizeSpaces(walkRequest.getWalkerId());
        boolean statusShowsWalker = FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(walkRequest.getStatus())
                || FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(walkRequest.getStatus());
        boolean shouldShowWalker = statusShowsWalker
                && !TextUtils.isEmpty(walkerId);
        holder.walkerNameTextView.setVisibility(shouldShowWalker ? View.VISIBLE : View.GONE);
        if (!shouldShowWalker) {
            return;
        }

        String walkerName = ValidationUtils.normalizeSpaces(walkerNamesById.get(walkerId));
        if (TextUtils.isEmpty(walkerName)) {
            walkerName = context.getString(R.string.selected_walker_fallback);
        }
        holder.walkerNameTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_walker_name),
                walkerName
        ));
    }

    private void bindActionButton(OwnerWalkRequestViewHolder holder, Context context, WalkRequest walkRequest) {
        holder.reviewSentTextView.setVisibility(View.GONE);

        if (Boolean.TRUE.equals(walkRequest.getReviewed())) {
            holder.reviewSentTextView.setVisibility(View.VISIBLE);
            holder.actionButton.setVisibility(View.GONE);
            holder.actionButton.setOnClickListener(null);
            return;
        }

        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(walkRequest.getStatus())) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setEnabled(actionsEnabled);
            holder.actionButton.setText(R.string.cancel_request);
            holder.actionButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onCancelRequest(walkRequest);
                }
            });
            return;
        }

        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(walkRequest.getStatus())) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setEnabled(actionsEnabled);
            holder.actionButton.setText(R.string.complete_request);
            holder.actionButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onCompleteRequest(walkRequest);
                }
            });
            return;
        }

        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(walkRequest.getStatus())) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setEnabled(actionsEnabled);
            holder.actionButton.setText(R.string.rate_walker);
            holder.actionButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onReviewWalker(walkRequest);
                }
            });
            return;
        }

        holder.actionButton.setVisibility(View.GONE);
        holder.actionButton.setOnClickListener(null);
    }

    private String getDisplayStatus(Context context, String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
            return context.getString(R.string.status_open_display);
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            return context.getString(R.string.status_accepted_display);
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            return context.getString(R.string.status_completed_display);
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            return context.getString(R.string.status_canceled_display);
        }
        return context.getString(R.string.status_unknown_display);
    }

    static class OwnerWalkRequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView dogNameTextView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        private final TextView durationTextView;
        private final TextView maxPriceTextView;
        private final TextView pickupLocationTextView;
        private final TextView statusTextView;
        private final TextView walkerNameTextView;
        private final TextView reviewSentTextView;
        private final Button actionButton;

        OwnerWalkRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            dogNameTextView = itemView.findViewById(R.id.text_dog_name);
            dateTextView = itemView.findViewById(R.id.text_date);
            timeTextView = itemView.findViewById(R.id.text_time);
            durationTextView = itemView.findViewById(R.id.text_duration);
            maxPriceTextView = itemView.findViewById(R.id.text_max_price);
            pickupLocationTextView = itemView.findViewById(R.id.text_pickup_location);
            statusTextView = itemView.findViewById(R.id.text_status);
            walkerNameTextView = itemView.findViewById(R.id.text_walker_name);
            reviewSentTextView = itemView.findViewById(R.id.text_review_sent);
            actionButton = itemView.findViewById(R.id.button_primary_action);
        }
    }
}
