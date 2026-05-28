package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.WalkerProfile;
import com.example.doggysitter.utils.AdminDisplayUtils;
import com.example.doggysitter.utils.PriceUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminWalkerProfileAdapter
        extends RecyclerView.Adapter<AdminWalkerProfileAdapter.AdminWalkerProfileViewHolder> {
    private final List<WalkerProfile> walkerProfiles = new ArrayList<>();

    public void submitList(List<WalkerProfile> newWalkerProfiles) {
        walkerProfiles.clear();
        walkerProfiles.addAll(newWalkerProfiles);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminWalkerProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_walker_profile, parent, false);
        return new AdminWalkerProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminWalkerProfileViewHolder holder, int position) {
        WalkerProfile profile = walkerProfiles.get(position);
        holder.fullNameTextView.setText("שם מלא: " + AdminDisplayUtils.fallbackText(profile.getFullName()));
        holder.phoneTextView.setText("טלפון: " + AdminDisplayUtils.fallbackText(profile.getPhone()));
        holder.descriptionTextView.setText("תיאור קצר: "
                + AdminDisplayUtils.fallbackText(profile.getDescription()));
        holder.experienceTextView.setText("שנות ניסיון: " + formatInteger(profile.getExperienceYears()));
        holder.priceTextView.setText("מחיר משוער לשעת טיול: "
                + formatShekelAmount(profile.getEstimatedHourlyPrice()));
        holder.radiusTextView.setText("רדיוס שירות: " + formatRadius(profile.getServiceRadiusKm()));
        holder.averageRatingTextView.setText("דירוג ממוצע: "
                + AdminDisplayUtils.formatRating(profile.getAverageRating()));
        holder.ratingCountTextView.setText("מספר דירוגים: " + formatLong(profile.getRatingCount()));
        holder.activeTextView.setText("סטטוס: " + AdminDisplayUtils.formatActive(profile.isActive()));
    }

    @Override
    public int getItemCount() {
        return walkerProfiles.size();
    }

    private String formatInteger(Integer value) {
        return value == null ? "לא צוין" : String.valueOf(value);
    }

    private String formatLong(Long value) {
        return value == null ? "0" : String.valueOf(value);
    }

    private String formatShekelAmount(Integer value) {
        return value == null ? "לא צוין" : PriceUtils.formatWholeShekelAmount(value) + " ₪";
    }

    private String formatRadius(Integer value) {
        return value == null ? "לא צוין" : value + " ק״מ";
    }

    static class AdminWalkerProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView fullNameTextView;
        private final TextView phoneTextView;
        private final TextView descriptionTextView;
        private final TextView experienceTextView;
        private final TextView priceTextView;
        private final TextView radiusTextView;
        private final TextView averageRatingTextView;
        private final TextView ratingCountTextView;
        private final TextView activeTextView;

        AdminWalkerProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.text_full_name);
            phoneTextView = itemView.findViewById(R.id.text_phone);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            experienceTextView = itemView.findViewById(R.id.text_experience);
            priceTextView = itemView.findViewById(R.id.text_price);
            radiusTextView = itemView.findViewById(R.id.text_radius);
            averageRatingTextView = itemView.findViewById(R.id.text_average_rating);
            ratingCountTextView = itemView.findViewById(R.id.text_rating_count);
            activeTextView = itemView.findViewById(R.id.text_active);
        }
    }
}
