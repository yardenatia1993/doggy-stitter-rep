package com.example.doggysitter.repositories;

import com.example.doggysitter.models.WalkerProfile;
import com.example.doggysitter.utils.FirestoreConstants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class WalkerProfileRepository {
    private final FirebaseFirestore firestore;

    public WalkerProfileRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<DocumentSnapshot> getProfile(String uid) {
        return firestore.collection(FirestoreConstants.COLLECTION_WALKER_PROFILES)
                .document(uid)
                .get();
    }

    public Task<Void> saveProfile(WalkerProfile profile, boolean profileExists) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put(FirestoreConstants.FIELD_UID, profile.getUid());
        profileData.put(FirestoreConstants.FIELD_FULL_NAME, profile.getFullName());
        profileData.put(FirestoreConstants.FIELD_PHONE, profile.getPhone());
        profileData.put(FirestoreConstants.FIELD_PHONE_PREFIX, profile.getPhonePrefix());
        profileData.put(FirestoreConstants.FIELD_PHONE_NUMBER, profile.getPhoneNumber());
        profileData.put(FirestoreConstants.FIELD_DESCRIPTION, profile.getDescription());
        profileData.put(FirestoreConstants.FIELD_EXPERIENCE_YEARS, profile.getExperienceYears());
        profileData.put(FirestoreConstants.FIELD_ESTIMATED_HOURLY_PRICE, profile.getEstimatedHourlyPrice());
        profileData.put(FirestoreConstants.FIELD_AVAILABLE_DAYS, profile.getAvailableDays());
        profileData.put(FirestoreConstants.FIELD_AVAILABILITY, profile.getAvailability());
        profileData.put(FirestoreConstants.FIELD_ACTIVE, profile.isActive());
        profileData.put(FirestoreConstants.FIELD_SERVICE_LAT, profile.getServiceLat());
        profileData.put(FirestoreConstants.FIELD_SERVICE_LNG, profile.getServiceLng());
        profileData.put(FirestoreConstants.FIELD_SERVICE_RADIUS_KM, profile.getServiceRadiusKm());
        profileData.put(FirestoreConstants.FIELD_SERVICE_LOCATION_LABEL, profile.getServiceLocationLabel());
        profileData.put(FirestoreConstants.FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        if (!profileExists) {
            profileData.put(FirestoreConstants.FIELD_CREATED_AT, FieldValue.serverTimestamp());
        }

        return firestore.collection(FirestoreConstants.COLLECTION_WALKER_PROFILES)
                .document(profile.getUid())
                .set(profileData, SetOptions.merge());
    }
}
