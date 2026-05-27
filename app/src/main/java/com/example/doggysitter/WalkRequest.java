package com.example.doggysitter;

public class WalkRequest {
    private String id;
    private String ownerUid;
    private String dogId;
    private String walkerUid;
    private String status;
    private String address;
    private String notes;
    private Object scheduledAt;
    private Object createdAt;

    public WalkRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getDogId() {
        return dogId;
    }

    public void setDogId(String dogId) {
        this.dogId = dogId;
    }

    public String getWalkerUid() {
        return walkerUid;
    }

    public void setWalkerUid(String walkerUid) {
        this.walkerUid = walkerUid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Object getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Object scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
}
