package com.example.doggysitter;

public final class FirestoreConstants {
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_DOGS = "dogs";
    public static final String COLLECTION_WALK_REQUESTS = "walkRequests";
    public static final String COLLECTION_WALKER_PROFILES = "walkerProfiles";
    public static final String COLLECTION_REVIEWS = "reviews";

    public static final String FIELD_UID = "uid";
    public static final String FIELD_FULL_NAME = "fullName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_ID = "id";
    public static final String FIELD_OWNER_ID = "ownerId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_AGE_MONTHS = "ageMonths";
    public static final String FIELD_BREED = "breed";
    public static final String FIELD_NOTES = "notes";
    public static final String FIELD_DOG_ID = "dogId";
    public static final String FIELD_DOG_NAME = "dogName";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_DURATION_MINUTES = "durationMinutes";
    public static final String FIELD_MAX_PRICE = "maxPrice";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_EXPERIENCE_YEARS = "experienceYears";
    public static final String FIELD_PRICE_PER_WALK = "pricePerWalk";
    public static final String FIELD_AVAILABLE_DAYS = "availableDays";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_WALKER_ID = "walkerId";
    public static final String FIELD_ACCEPTED_AT = "acceptedAt";

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_WALKER = "WALKER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String WALK_REQUEST_STATUS_OPEN = "OPEN";
    public static final String WALK_REQUEST_STATUS_ACCEPTED = "ACCEPTED";

    private FirestoreConstants() {
    }
}
