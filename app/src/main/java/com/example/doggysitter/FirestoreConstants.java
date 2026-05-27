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

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_WALKER = "WALKER";
    public static final String ROLE_ADMIN = "ADMIN";

    private FirestoreConstants() {
    }
}
