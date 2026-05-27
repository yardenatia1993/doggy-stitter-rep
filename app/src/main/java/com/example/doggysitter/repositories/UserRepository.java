package com.example.doggysitter.repositories;

import com.example.doggysitter.models.User;
import com.example.doggysitter.utils.FirestoreConstants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore firestore;

    public UserRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<Void> createUser(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put(FirestoreConstants.FIELD_UID, user.getUid());
        userData.put(FirestoreConstants.FIELD_FULL_NAME, user.getFullName());
        userData.put(FirestoreConstants.FIELD_EMAIL, user.getEmail());
        userData.put(FirestoreConstants.FIELD_ROLE, user.getRole());
        userData.put(FirestoreConstants.FIELD_CREATED_AT, FieldValue.serverTimestamp());

        return firestore.collection(FirestoreConstants.COLLECTION_USERS)
                .document(user.getUid())
                .set(userData, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getUser(String uid) {
        return firestore.collection(FirestoreConstants.COLLECTION_USERS)
                .document(uid)
                .get();
    }

    public Task<Void> saveRole(String uid, String role) {
        Map<String, Object> roleData = new HashMap<>();
        roleData.put(FirestoreConstants.FIELD_ROLE, role);

        return firestore.collection(FirestoreConstants.COLLECTION_USERS)
                .document(uid)
                .set(roleData, SetOptions.merge());
    }
}
