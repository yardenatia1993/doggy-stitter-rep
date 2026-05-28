package com.example.doggysitter.repositories;

import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.utils.FirestoreConstants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class WalkRequestRepository {
    private final CollectionReference walkRequestsCollection;

    public WalkRequestRepository() {
        walkRequestsCollection = FirebaseFirestore.getInstance()
                .collection(FirestoreConstants.COLLECTION_WALK_REQUESTS);
    }

    public Task<Void> createWalkRequest(WalkRequest walkRequest) {
        String requestId = walkRequestsCollection.document().getId();
        walkRequest.setId(requestId);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put(FirestoreConstants.FIELD_ID, walkRequest.getId());
        requestData.put(FirestoreConstants.FIELD_OWNER_ID, walkRequest.getOwnerId());
        requestData.put(FirestoreConstants.FIELD_DOG_ID, walkRequest.getDogId());
        requestData.put(FirestoreConstants.FIELD_DOG_NAME, walkRequest.getDogName());
        requestData.put(FirestoreConstants.FIELD_DATE, walkRequest.getDate());
        requestData.put(FirestoreConstants.FIELD_TIME, walkRequest.getTime());
        requestData.put(FirestoreConstants.FIELD_DURATION_MINUTES, walkRequest.getDurationMinutes());
        requestData.put(FirestoreConstants.FIELD_MAX_PRICE, walkRequest.getMaxPrice());
        requestData.put(FirestoreConstants.FIELD_NOTES, walkRequest.getNotes());
        requestData.put(FirestoreConstants.FIELD_STATUS, walkRequest.getStatus());
        requestData.put(FirestoreConstants.FIELD_PICKUP_LAT, walkRequest.getPickupLat());
        requestData.put(FirestoreConstants.FIELD_PICKUP_LNG, walkRequest.getPickupLng());
        requestData.put(FirestoreConstants.FIELD_PICKUP_LOCATION_LABEL, walkRequest.getPickupLocationLabel());
        requestData.put(FirestoreConstants.FIELD_CREATED_AT, FieldValue.serverTimestamp());

        return walkRequestsCollection.document(requestId).set(requestData);
    }

    public Task<QuerySnapshot> getOpenWalkRequests() {
        return walkRequestsCollection
                .whereEqualTo(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_OPEN)
                .get();
    }

    public Task<QuerySnapshot> getRequestsForOwner(String ownerId) {
        return walkRequestsCollection
                .whereEqualTo(FirestoreConstants.FIELD_OWNER_ID, ownerId)
                .get();
    }

    public Task<QuerySnapshot> getAcceptedWalkRequestsForWalker(String walkerId) {
        return walkRequestsCollection
                .whereEqualTo(FirestoreConstants.FIELD_WALKER_ID, walkerId)
                .get();
    }

    public Task<Void> acceptWalkRequest(String requestId, String walkerId) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED);
        requestData.put(FirestoreConstants.FIELD_WALKER_ID, walkerId);
        requestData.put(FirestoreConstants.FIELD_ACCEPTED_AT, FieldValue.serverTimestamp());

        return walkRequestsCollection.document(requestId).update(requestData);
    }

    public Task<Void> cancelRequest(String requestId) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_CANCELED);
        requestData.put(FirestoreConstants.FIELD_CANCELED_AT, FieldValue.serverTimestamp());

        return walkRequestsCollection.document(requestId).update(requestData);
    }

    public Task<Void> completeRequest(String requestId) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED);
        requestData.put(FirestoreConstants.FIELD_COMPLETED_AT, FieldValue.serverTimestamp());

        return walkRequestsCollection.document(requestId).update(requestData);
    }
}
