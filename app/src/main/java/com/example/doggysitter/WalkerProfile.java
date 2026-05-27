package com.example.doggysitter;

public class WalkerProfile {
    private String uid;
    private String bio;
    private int pricePerWalk;
    private boolean available;
    private Object createdAt;

    public WalkerProfile() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getPricePerWalk() {
        return pricePerWalk;
    }

    public void setPricePerWalk(int pricePerWalk) {
        this.pricePerWalk = pricePerWalk;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
}
