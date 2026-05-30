package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.AdminUserAdapter;
import com.example.doggysitter.models.User;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AdminBaseActivity {
    private AdminRepository adminRepository;
    private AdminUserAdapter adapter;
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

        titleTextView.setText(R.string.users);
        emptyTextView.setText(R.string.no_admin_users);
        adapter = new AdminUserAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        requireAdminAccess(this::loadUsers);
    }

    private void loadUsers() {
        setLoading(true);
        adminRepository.getAllUsers()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(user.getUid()))) {
                                user.setUid(documentSnapshot.getId());
                            }
                            users.add(user);
                        }
                    }
                    setLoading(false);
                    adapter.submitList(users);
                    updateEmptyState(users.isEmpty());
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    handleAdminDataLoadFailure("Failed to load admin users", error);
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
