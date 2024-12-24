package com.example.doggysitter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private FirebaseFirestore db;
    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getUser(String userId, OnSuccessListener<DocumentSnapshot> listener) {
        db.collection("users").document(userId).get().addOnSuccessListener(listener);
    }

}
