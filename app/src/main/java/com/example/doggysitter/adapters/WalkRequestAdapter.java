package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.LocationUtils;
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
import java.util.List;
import java.util.Locale;

public class WalkRequestAdapter extends RecyclerView.Adapter<WalkRequestAdapter.WalkRequestViewHolder> {
    public interface Listener {
        void onAcceptWalkRequest(WalkRequest walkRequest);
    }

    private final List<WalkRequest> walkRequests = new ArrayList<>();
    private final Listener listener;
    private final boolean showAcceptButton;
    private final boolean showStatus;

    public WalkRequestAdapter(Listener listener, boolean showAcceptButton, boolean showStatus) {
        this.listener = listener;
        this.showAcceptButton = showAcceptButton;
        this.showStatus = showStatus;
    }

    public void submitList(List<WalkRequest> newWalkRequests) {
        walkRequests.clear();
        walkRequests.addAll(newWalkRequests);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalkRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_walk_request, parent, false);
        return new WalkRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalkRequestViewHolder holder, int position) {
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
                walkRequest.getMaxPrice()
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

        Double distanceKm = walkRequest.getDistanceKm();
        holder.distanceTextView.setVisibility(distanceKm == null ? View.GONE : View.VISIBLE);
        if (distanceKm != null) {
            holder.distanceTextView.setText(String.format(
                    Locale.getDefault(),
                    context.getString(R.string.request_distance),
                    LocationUtils.formatDistanceHebrew(distanceKm)
            ));
        }

        String notes = ValidationUtils.normalizeSpaces(walkRequest.getNotes());
        holder.notesTextView.setVisibility(TextUtils.isEmpty(notes) ? View.GONE : View.VISIBLE);
        holder.notesTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.request_notes),
                notes
        ));

        holder.statusTextView.setVisibility(showStatus ? View.VISIBLE : View.GONE);
        holder.statusTextView.setText(String.format(
                Locale.getDefault(),
                context.getString(R.string.status_label),
                getDisplayStatus(context, walkRequest.getStatus())
        ));

        holder.acceptButton.setVisibility(showAcceptButton ? View.VISIBLE : View.GONE);
        holder.acceptButton.setOnClickListener(view -> {
            if (listener != null) {
                listener.onAcceptWalkRequest(walkRequest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return walkRequests.size();
    }

    private String getDisplayStatus(Context context, String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            return context.getString(R.string.status_accepted_display);
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
            return context.getString(R.string.status_open_display);
        }
        return context.getString(R.string.status_unknown_display);
    }

    static class WalkRequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView dogNameTextView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        private final TextView durationTextView;
        private final TextView maxPriceTextView;
        private final TextView pickupLocationTextView;
        private final TextView distanceTextView;
        private final TextView notesTextView;
        private final TextView statusTextView;
        private final Button acceptButton;

        WalkRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            dogNameTextView = itemView.findViewById(R.id.text_dog_name);
            dateTextView = itemView.findViewById(R.id.text_date);
            timeTextView = itemView.findViewById(R.id.text_time);
            durationTextView = itemView.findViewById(R.id.text_duration);
            maxPriceTextView = itemView.findViewById(R.id.text_max_price);
            pickupLocationTextView = itemView.findViewById(R.id.text_pickup_location);
            distanceTextView = itemView.findViewById(R.id.text_distance);
            notesTextView = itemView.findViewById(R.id.text_notes);
            statusTextView = itemView.findViewById(R.id.text_status);
            acceptButton = itemView.findViewById(R.id.button_accept);
        }
    }
}
