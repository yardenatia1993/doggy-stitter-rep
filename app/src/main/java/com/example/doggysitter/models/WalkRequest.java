package com.example.doggysitter.models;


public class WalkRequest {
    private String id;
    private String ownerId;
    private String dogId;
    private String dogName;
    private String date;
    private String time;
    private Object startAt;
    private int durationMinutes;
    private double maxPrice;
    private String notes;
    private String status;
    private String walkerId;
    private Boolean reviewed;
    private String reviewId;
    private Double pickupLat;
    private Double pickupLng;
    private String pickupLocationLabel;
    private Double distanceKm;
    private Object createdAt;
    private Object acceptedAt;
    private Object completedAt;
    private Object canceledAt;

    public WalkRequest() {
    }

    public WalkRequest(String id, String ownerId, String dogId, String dogName, String date,
                       String time, int durationMinutes, double maxPrice, String notes,
                       String status) {
        this.id = id;
        this.ownerId = ownerId;
        this.dogId = dogId;
        this.dogName = dogName;
        this.date = date;
        this.time = time;
        this.durationMinutes = durationMinutes;
        this.maxPrice = maxPrice;
        this.notes = notes;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getDogId() {
        return dogId;
    }

    public void setDogId(String dogId) {
        this.dogId = dogId;
    }

    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getStartAt() {
        return startAt;
    }

    public void setStartAt(Object startAt) {
        this.startAt = startAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWalkerId() {
        return walkerId;
    }

    public void setWalkerId(String walkerId) {
        this.walkerId = walkerId;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public String getPickupLocationLabel() {
        return pickupLocationLabel;
    }

    public void setPickupLocationLabel(String pickupLocationLabel) {
        this.pickupLocationLabel = pickupLocationLabel;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public Object getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Object acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Object getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Object completedAt) {
        this.completedAt = completedAt;
    }

    public Object getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Object canceledAt) {
        this.canceledAt = canceledAt;
    }
}
