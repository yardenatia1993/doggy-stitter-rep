package com.example.doggysitter.repositories;

import com.example.doggysitter.utils.FirestoreConstants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminRepository {
    private final FirebaseFirestore firestore;

    public AdminRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> getAllUsers() {
        return firestore.collection(FirestoreConstants.COLLECTION_USERS).get();
    }

    public Task<QuerySnapshot> getAllWalkerProfiles() {
        return firestore.collection(FirestoreConstants.COLLECTION_WALKER_PROFILES).get();
    }

    public Task<QuerySnapshot> getAllDogs() {
        return firestore.collection(FirestoreConstants.COLLECTION_DOGS).get();
    }

    public Task<QuerySnapshot> getAllWalkRequests() {
        return firestore.collection(FirestoreConstants.COLLECTION_WALK_REQUESTS).get();
    }

    public Task<QuerySnapshot> getAllReviews() {
        return firestore.collection(FirestoreConstants.COLLECTION_REVIEWS).get();
    }
}
