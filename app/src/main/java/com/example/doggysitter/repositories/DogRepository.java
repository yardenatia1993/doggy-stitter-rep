package com.example.doggysitter.repositories;

import com.example.doggysitter.models.Dog;
import com.example.doggysitter.utils.FirestoreConstants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DogRepository {
    private final CollectionReference dogsCollection;

    public DogRepository() {
        dogsCollection = FirebaseFirestore.getInstance()
                .collection(FirestoreConstants.COLLECTION_DOGS);
    }

    public Task<Void> addDog(Dog dog) {
        String dogId = dogsCollection.document().getId();
        dog.setId(dogId);

        Map<String, Object> dogData = new HashMap<>();
        dogData.put(FirestoreConstants.FIELD_ID, dog.getId());
        dogData.put(FirestoreConstants.FIELD_OWNER_ID, dog.getOwnerId());
        dogData.put(FirestoreConstants.FIELD_NAME, dog.getName());
        dogData.put(FirestoreConstants.FIELD_AGE_MONTHS, dog.getAgeMonths());
        dogData.put(FirestoreConstants.FIELD_BREED, dog.getBreed());
        dogData.put(FirestoreConstants.FIELD_NOTES, dog.getNotes());
        dogData.put(FirestoreConstants.FIELD_CREATED_AT, FieldValue.serverTimestamp());
        dogData.put(FirestoreConstants.FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        return dogsCollection.document(dogId).set(dogData);
    }

    public Task<DocumentSnapshot> getDog(String dogId) {
        return dogsCollection.document(dogId).get();
    }

    public Task<Void> updateDog(String dogId, String name, int ageMonths, String breed, String notes) {
        Map<String, Object> dogData = new HashMap<>();
        dogData.put(FirestoreConstants.FIELD_NAME, name);
        dogData.put(FirestoreConstants.FIELD_AGE_MONTHS, ageMonths);
        dogData.put(FirestoreConstants.FIELD_BREED, breed);
        dogData.put(FirestoreConstants.FIELD_NOTES, notes);
        dogData.put(FirestoreConstants.FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        return dogsCollection.document(dogId).update(dogData);
    }

    public Task<Void> deleteDog(String dogId) {
        return dogsCollection.document(dogId).delete();
    }

    public Task<QuerySnapshot> getDogsForOwner(String ownerId) {
        return dogsCollection
                .whereEqualTo(FirestoreConstants.FIELD_OWNER_ID, ownerId)
                .get();
    }
}
