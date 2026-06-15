package com.example.doggysitter.activities.admin;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.AdminUserAdapter;
import com.example.doggysitter.models.User;
import com.example.doggysitter.repositories.AdminRepository;
import com.example.doggysitter.utils.FirestoreConstants;
import com.example.doggysitter.utils.ValidationUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AdminBaseActivity {
    private static final int FILTER_ALL = 0;
    private static final int FILTER_OWNERS = 1;
    private static final int FILTER_WALKERS = 2;

    private AdminRepository adminRepository;
    private AdminUserAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private Spinner roleFilterSpinner;
    private final List<User> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        adminRepository = new AdminRepository();
        recyclerView = findViewById(R.id.recycler_users);
        emptyTextView = findViewById(R.id.text_empty);
        progressBar = findViewById(R.id.progress_bar);
        roleFilterSpinner = findViewById(R.id.spinner_role_filter);

        adapter = new AdminUserAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setupRoleFilter();
        requireAdminAccess(this::loadUsers);
    }

    private void setupRoleFilter() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.admin_user_role_filters,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleFilterSpinner.setAdapter(spinnerAdapter);
        roleFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyRoleFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyRoleFilter();
            }
        });
    }

    private void loadUsers() {
        setLoading(true);
        adminRepository.getAllUsers()
                .addOnSuccessListener(querySnapshot -> {
                    allUsers.clear();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (TextUtils.isEmpty(ValidationUtils.normalizeSpaces(user.getUid()))) {
                                user.setUid(documentSnapshot.getId());
                            }
                            allUsers.add(user);
                        }
                    }
                    setLoading(false);
                    applyRoleFilter();
                })
                .addOnFailureListener(error -> {
                    setLoading(false);
                    handleAdminDataLoadFailure("Failed to load admin users", error);
                });
    }

    private void applyRoleFilter() {
        List<User> filteredUsers = new ArrayList<>();
        int filter = roleFilterSpinner.getSelectedItemPosition();
        for (User user : allUsers) {
            if (matchesFilter(user, filter)) {
                filteredUsers.add(user);
            }
        }
        adapter.submitList(filteredUsers);
        updateEmptyState(filteredUsers.isEmpty());
    }

    private boolean matchesFilter(User user, int filter) {
        if (filter == FILTER_OWNERS) {
            return FirestoreConstants.ROLE_OWNER.equals(user.getRole());
        }
        if (filter == FILTER_WALKERS) {
            return FirestoreConstants.ROLE_WALKER.equals(user.getRole());
        }
        return true;
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            emptyTextView.setVisibility(View.GONE);
        }
    }
}
