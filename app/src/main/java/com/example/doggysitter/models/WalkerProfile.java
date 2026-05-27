package com.example.doggysitter.models;


import java.util.Map;
import java.util.List;

public class WalkerProfile {
    private String uid;
    private String fullName;
    private String phone;
    private String phonePrefix;
    private String phoneNumber;
    private String description;
    private Integer experienceYears;
    private Double pricePerWalk;
    private Integer estimatedHourlyPrice;
    private List<String> availableDays;
    private Map<String, Map<String, String>> availability;
    private boolean active;
    private Double serviceLat;
    private Double serviceLng;
    private Integer serviceRadiusKm;
    private String serviceLocationLabel;
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

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public Integer getEstimatedHourlyPrice() {
        return estimatedHourlyPrice;
    }

    public void setEstimatedHourlyPrice(Integer estimatedHourlyPrice) {
        this.estimatedHourlyPrice = estimatedHourlyPrice;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }

    public Map<String, Map<String, String>> getAvailability() {
        return availability;
    }

    public void setAvailability(Map<String, Map<String, String>> availability) {
        this.availability = availability;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Double getServiceLat() {
        return serviceLat;
    }

    public void setServiceLat(Double serviceLat) {
        this.serviceLat = serviceLat;
    }

    public Double getServiceLng() {
        return serviceLng;
    }

    public void setServiceLng(Double serviceLng) {
        this.serviceLng = serviceLng;
    }

    public Integer getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(Integer serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }

    public String getServiceLocationLabel() {
        return serviceLocationLabel;
    }

    public void setServiceLocationLabel(String serviceLocationLabel) {
        this.serviceLocationLabel = serviceLocationLabel;
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
