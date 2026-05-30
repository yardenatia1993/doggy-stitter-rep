package com.example.doggysitter.repositories;

import com.example.doggysitter.utils.FirestoreConstants;

import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ReviewRepository {
    private final FirebaseFirestore firestore;

    public ReviewRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<Void> submitReview(String requestId, String ownerId, int rating, String comment) {
        DocumentReference requestReference = firestore
                .collection(FirestoreConstants.COLLECTION_WALK_REQUESTS)
                .document(requestId);
        DocumentReference reviewReference = firestore
                .collection(FirestoreConstants.COLLECTION_REVIEWS)
                .document();
        String reviewId = reviewReference.getId();

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot requestSnapshot = transaction.get(requestReference);
            if (!requestSnapshot.exists()) {
                throw abort("בקשת הטיול לא נמצאה");
            }

            String requestOwnerId = requestSnapshot.getString(FirestoreConstants.FIELD_OWNER_ID);
            if (!ownerId.equals(requestOwnerId)) {
                throw abort("אין הרשאה לדרג בקשה זו");
            }

            String status = requestSnapshot.getString(FirestoreConstants.FIELD_STATUS);
            if (!FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
                throw abort("ניתן לדרג רק טיול שהושלם");
            }

            if (Boolean.TRUE.equals(requestSnapshot.getBoolean(FirestoreConstants.FIELD_REVIEWED))) {
                throw abort("כבר נשלח דירוג עבור הבקשה");
            }

            String walkerId = requestSnapshot.getString(FirestoreConstants.FIELD_WALKER_ID);
            if (TextUtils.isEmpty(walkerId)) {
                throw abort("לא נמצא דוגווקר עבור הבקשה");
            }

            if (rating < 1 || rating > 5) {
                throw abort("יש לבחור דירוג בין 1 ל-5");
            }

            DocumentReference walkerProfileReference = firestore
                    .collection(FirestoreConstants.COLLECTION_WALKER_PROFILES)
                    .document(walkerId);
            DocumentSnapshot walkerProfileSnapshot = transaction.get(walkerProfileReference);
            if (!walkerProfileSnapshot.exists()) {
                throw abort("פרופיל הדוגווקר לא נמצא");
            }

            long ratingSum = getLongValue(walkerProfileSnapshot, FirestoreConstants.FIELD_RATING_SUM);
            long ratingCount = getLongValue(walkerProfileSnapshot, FirestoreConstants.FIELD_RATING_COUNT);
            long updatedRatingSum = ratingSum + rating;
            long updatedRatingCount = ratingCount + 1;
            double averageRating = (double) updatedRatingSum / updatedRatingCount;

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put(FirestoreConstants.FIELD_ID, reviewId);
            reviewData.put(FirestoreConstants.FIELD_REQUEST_ID, requestId);
            reviewData.put(FirestoreConstants.FIELD_OWNER_ID, ownerId);
            reviewData.put(FirestoreConstants.FIELD_WALKER_ID, walkerId);
            reviewData.put(FirestoreConstants.FIELD_RATING, rating);
            reviewData.put(FirestoreConstants.FIELD_COMMENT, comment);
            reviewData.put(FirestoreConstants.FIELD_CREATED_AT, FieldValue.serverTimestamp());

            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FirestoreConstants.FIELD_REVIEWED, true);
            requestData.put(FirestoreConstants.FIELD_REVIEW_ID, reviewId);

            Map<String, Object> profileData = new HashMap<>();
            profileData.put(FirestoreConstants.FIELD_RATING_SUM, updatedRatingSum);
            profileData.put(FirestoreConstants.FIELD_RATING_COUNT, updatedRatingCount);
            profileData.put(FirestoreConstants.FIELD_AVERAGE_RATING, averageRating);

            transaction.set(reviewReference, reviewData);
            transaction.update(requestReference, requestData);
            transaction.set(walkerProfileReference, profileData, SetOptions.merge());
            return null;
        });
    }

    private FirebaseFirestoreException abort(String message) {
        return new FirebaseFirestoreException(message, FirebaseFirestoreException.Code.ABORTED);
    }

    private long getLongValue(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
