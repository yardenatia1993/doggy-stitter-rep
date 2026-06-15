package com.example.doggysitter.utils;


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
    public static final String FIELD_REQUEST_ID = "requestId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_AGE_MONTHS = "ageMonths";
    public static final String FIELD_BREED = "breed";
    public static final String FIELD_NOTES = "notes";
    public static final String FIELD_DOG_ID = "dogId";
    public static final String FIELD_DOG_NAME = "dogName";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_START_AT = "startAt";
    public static final String FIELD_DURATION_MINUTES = "durationMinutes";
    public static final String FIELD_MAX_PRICE = "maxPrice";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_EXPERIENCE_YEARS = "experienceYears";
    public static final String FIELD_PRICE_PER_WALK = "pricePerWalk";
    public static final String FIELD_ESTIMATED_HOURLY_PRICE = "estimatedHourlyPrice";
    public static final String FIELD_AVAILABLE_DAYS = "availableDays";
    public static final String FIELD_AVAILABILITY = "availability";
    public static final String FIELD_AVAILABILITY_START = "start";
    public static final String FIELD_AVAILABILITY_END = "end";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_WALKER_ID = "walkerId";
    public static final String FIELD_ACCEPTED_AT = "acceptedAt";
    public static final String FIELD_COMPLETED_AT = "completedAt";
    public static final String FIELD_CANCELED_AT = "canceledAt";
    public static final String FIELD_REVIEWED = "reviewed";
    public static final String FIELD_REVIEW_ID = "reviewId";
    public static final String FIELD_RATING = "rating";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_RATING_SUM = "ratingSum";
    public static final String FIELD_RATING_COUNT = "ratingCount";
    public static final String FIELD_AVERAGE_RATING = "averageRating";
    public static final String FIELD_PICKUP_LAT = "pickupLat";
    public static final String FIELD_PICKUP_LNG = "pickupLng";
    public static final String FIELD_PICKUP_LOCATION_LABEL = "pickupLocationLabel";
    public static final String FIELD_SERVICE_LAT = "serviceLat";
    public static final String FIELD_SERVICE_LNG = "serviceLng";
    public static final String FIELD_SERVICE_RADIUS_KM = "serviceRadiusKm";
    public static final String FIELD_SERVICE_LOCATION_LABEL = "serviceLocationLabel";
    public static final String FIELD_PHONE_PREFIX = "phonePrefix";
    public static final String FIELD_PHONE_NUMBER = "phoneNumber";

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_WALKER = "WALKER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String WALK_REQUEST_STATUS_OPEN = "OPEN";
    public static final String WALK_REQUEST_STATUS_ACCEPTED = "ACCEPTED";
    public static final String WALK_REQUEST_STATUS_COMPLETED = "COMPLETED";
    public static final String WALK_REQUEST_STATUS_CANCELED = "CANCELED";

    public static final String DAY_SUNDAY = "SUNDAY";
    public static final String DAY_MONDAY = "MONDAY";
    public static final String DAY_TUESDAY = "TUESDAY";
    public static final String DAY_WEDNESDAY = "WEDNESDAY";
    public static final String DAY_THURSDAY = "THURSDAY";
    public static final String DAY_FRIDAY = "FRIDAY";
    public static final String DAY_SATURDAY = "SATURDAY";

    private FirestoreConstants() {
    }
}
