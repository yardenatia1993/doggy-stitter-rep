package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.User;
import com.example.doggysitter.utils.AdminDisplayUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder> {
    private final List<User> users = new ArrayList<>();

    public void submitList(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new AdminUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        User user = users.get(position);
        holder.fullNameTextView.setText("שם מלא: " + AdminDisplayUtils.fallbackText(user.getFullName()));
        holder.emailTextView.setText("אימייל: " + AdminDisplayUtils.fallbackText(user.getEmail()));
        holder.roleTextView.setText("תפקיד: " + AdminDisplayUtils.formatRole(user.getRole()));
        holder.createdAtTextView.setText("תאריך יצירה: " + AdminDisplayUtils.formatCreatedAt(user.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class AdminUserViewHolder extends RecyclerView.ViewHolder {
        private final TextView fullNameTextView;
        private final TextView emailTextView;
        private final TextView roleTextView;
        private final TextView createdAtTextView;

        AdminUserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.text_full_name);
            emailTextView = itemView.findViewById(R.id.text_email);
            roleTextView = itemView.findViewById(R.id.text_role);
            createdAtTextView = itemView.findViewById(R.id.text_created_at);
        }
    }
}
