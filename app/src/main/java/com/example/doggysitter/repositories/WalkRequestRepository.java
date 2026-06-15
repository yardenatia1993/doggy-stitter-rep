package com.example.doggysitter.repositories;

import com.example.doggysitter.models.WalkRequest;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.FirestoreConstants;

import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WalkRequestRepository {
    private final FirebaseFirestore firestore;
    private final CollectionReference walkRequestsCollection;

    public WalkRequestRepository() {
        firestore = FirebaseFirestore.getInstance();
        walkRequestsCollection = firestore
                .collection(FirestoreConstants.COLLECTION_WALK_REQUESTS);
    }

    public Task<Void> createWalkRequest(WalkRequest walkRequest) {
        String requestId = walkRequestsCollection.document().getId();
        walkRequest.setId(requestId);

        Date requestStartAt = DateTimeUtils.parseRequestDateTime(
                walkRequest.getDate(),
                walkRequest.getTime()
        );
        if (requestStartAt == null) {
            return Tasks.forException(abort("מועד הטיול אינו תקין"));
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put(FirestoreConstants.FIELD_ID, walkRequest.getId());
        requestData.put(FirestoreConstants.FIELD_OWNER_ID, walkRequest.getOwnerId());
        requestData.put(FirestoreConstants.FIELD_DOG_ID, walkRequest.getDogId());
        requestData.put(FirestoreConstants.FIELD_DOG_NAME, walkRequest.getDogName());
        requestData.put(FirestoreConstants.FIELD_DATE, walkRequest.getDate());
        requestData.put(FirestoreConstants.FIELD_TIME, walkRequest.getTime());
        requestData.put(FirestoreConstants.FIELD_START_AT, new Timestamp(requestStartAt));
        requestData.put(FirestoreConstants.FIELD_DURATION_MINUTES, walkRequest.getDurationMinutes());
        requestData.put(FirestoreConstants.FIELD_MAX_PRICE, walkRequest.getMaxPrice());
        requestData.put(FirestoreConstants.FIELD_NOTES, walkRequest.getNotes());
        requestData.put(FirestoreConstants.FIELD_STATUS, walkRequest.getStatus());
        requestData.put(FirestoreConstants.FIELD_REVIEWED, false);
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

    public Task<DocumentSnapshot> getRequest(String requestId) {
        return walkRequestsCollection.document(requestId).get();
    }

    public Task<QuerySnapshot> getAcceptedWalkRequestsForWalker(String walkerId) {
        return walkRequestsCollection
                .whereEqualTo(FirestoreConstants.FIELD_WALKER_ID, walkerId)
                .get();
    }

    public Task<Void> acceptWalkRequest(String requestId, String walkerId) {
        if (TextUtils.isEmpty(requestId) || TextUtils.isEmpty(walkerId)) {
            return Tasks.forException(abort("לא ניתן לקבל את הבקשה. יש להתחבר מחדש ולנסות שוב"));
        }

        DocumentReference requestReference = walkRequestsCollection.document(requestId);
        return firestore.runTransaction(transaction -> {
            DocumentSnapshot requestSnapshot = transaction.get(requestReference);
            if (!requestSnapshot.exists()) {
                throw abort("בקשת הטיול לא נמצאה");
            }

            String status = getStringValue(requestSnapshot, FirestoreConstants.FIELD_STATUS);
            if (!FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
                throw abort(getAcceptErrorMessage(status));
            }
            if (isRequestExpired(requestSnapshot)) {
                throw abort("לא ניתן לקבל בקשה שמועד הטיול שלה עבר");
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED);
            requestData.put(FirestoreConstants.FIELD_WALKER_ID, walkerId);
            requestData.put(FirestoreConstants.FIELD_ACCEPTED_AT, FieldValue.serverTimestamp());
            transaction.update(requestReference, requestData);
            return null;
        });
    }

    public Task<Void> cancelRequest(String requestId, String ownerId) {
        if (TextUtils.isEmpty(requestId) || TextUtils.isEmpty(ownerId)) {
            return Tasks.forException(abort("לא ניתן לבטל את הבקשה. יש להתחבר מחדש ולנסות שוב"));
        }

        DocumentReference requestReference = walkRequestsCollection.document(requestId);
        return firestore.runTransaction(transaction -> {
            DocumentSnapshot requestSnapshot = transaction.get(requestReference);
            if (!requestSnapshot.exists()) {
                throw abort("בקשת הטיול לא נמצאה");
            }
            if (!ownerId.equals(getStringValue(requestSnapshot, FirestoreConstants.FIELD_OWNER_ID))) {
                throw abort("אין הרשאה לבטל בקשה זו");
            }

            String status = getStringValue(requestSnapshot, FirestoreConstants.FIELD_STATUS);
            if (!FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
                throw abort(getCancelErrorMessage(status));
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_CANCELED);
            requestData.put(FirestoreConstants.FIELD_CANCELED_AT, FieldValue.serverTimestamp());
            transaction.update(requestReference, requestData);
            return null;
        });
    }

    public Task<Void> completeRequest(String requestId, String walkerId) {
        if (TextUtils.isEmpty(requestId) || TextUtils.isEmpty(walkerId)) {
            return Tasks.forException(abort("לא ניתן להשלים את הטיול. יש להתחבר מחדש ולנסות שוב"));
        }

        DocumentReference requestReference = walkRequestsCollection.document(requestId);
        return firestore.runTransaction(transaction -> {
            DocumentSnapshot requestSnapshot = transaction.get(requestReference);
            if (!requestSnapshot.exists()) {
                throw abort("בקשת הטיול לא נמצאה");
            }
            if (!walkerId.equals(getStringValue(requestSnapshot, FirestoreConstants.FIELD_WALKER_ID))) {
                throw abort("רק הדוגווקר שקיבל את הבקשה יכול להשלים אותה");
            }

            String status = getStringValue(requestSnapshot, FirestoreConstants.FIELD_STATUS);
            if (!FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
                throw abort(getCompleteErrorMessage(status));
            }

            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FirestoreConstants.FIELD_STATUS, FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED);
            requestData.put(FirestoreConstants.FIELD_COMPLETED_AT, FieldValue.serverTimestamp());
            transaction.update(requestReference, requestData);
            return null;
        });
    }

    private String getAcceptErrorMessage(String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            return "הבקשה כבר התקבלה על ידי דוגווקר אחר";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            return "הבקשה כבר בוטלה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            return "הבקשה כבר הושלמה";
        }
        return "לא ניתן לקבל בקשה במצב הנוכחי";
    }

    private String getCancelErrorMessage(String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_ACCEPTED.equals(status)) {
            return "לא ניתן לבטל בקשה שכבר התקבלה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            return "הבקשה כבר בוטלה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            return "לא ניתן לבטל טיול שכבר הושלם";
        }
        return "לא ניתן לבטל בקשה במצב הנוכחי";
    }

    private String getCompleteErrorMessage(String status) {
        if (FirestoreConstants.WALK_REQUEST_STATUS_OPEN.equals(status)) {
            return "לא ניתן להשלים בקשה שעדיין פתוחה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_CANCELED.equals(status)) {
            return "לא ניתן להשלים בקשה שבוטלה";
        }
        if (FirestoreConstants.WALK_REQUEST_STATUS_COMPLETED.equals(status)) {
            return "הטיול כבר סומן כהושלם";
        }
        return "לא ניתן להשלים בקשה במצב הנוכחי";
    }

    private boolean isRequestExpired(DocumentSnapshot requestSnapshot) {
        Object startAt = requestSnapshot.get(FirestoreConstants.FIELD_START_AT);
        if (startAt instanceof Timestamp) {
            return ((Timestamp) startAt).toDate().getTime() <= System.currentTimeMillis();
        }

        String date = getStringValue(requestSnapshot, FirestoreConstants.FIELD_DATE);
        String time = getStringValue(requestSnapshot, FirestoreConstants.FIELD_TIME);
        return DateTimeUtils.isPastDateTime(date, time);
    }

    private String getStringValue(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);
        return value instanceof String ? (String) value : null;
    }

    private FirebaseFirestoreException abort(String message) {
        return new FirebaseFirestoreException(message, FirebaseFirestoreException.Code.ABORTED);
    }
}
