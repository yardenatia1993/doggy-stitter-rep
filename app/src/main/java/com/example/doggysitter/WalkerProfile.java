package com.example.doggysitter;

import java.util.List;

public class WalkerProfile {
    private String uid;
    private String fullName;
    private String phone;
    private String description;
    private Integer experienceYears;
    private Double pricePerWalk;
    private List<String> availableDays;
    private boolean active;
    private Object createdAt;
    private Object updatedAt;

    public WalkerProfile() {
    }

    public WalkerProfile(String uid, String fullName, String phone, String description,
                         Integer experienceYears, Double pricePerWalk, List<String> availableDays,
                         boolean active) {
        this.uid = uid;
        this.fullName = fullName;
        this.phone = phone;
        this.description = description;
        this.experienceYears = experienceYears;
        this.pricePerWalk = pricePerWalk;
        this.availableDays = availableDays;
        this.active = active;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Double getPricePerWalk() {
        return pricePerWalk;
    }

    public void setPricePerWalk(Double pricePerWalk) {
        this.pricePerWalk = pricePerWalk;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }
}
