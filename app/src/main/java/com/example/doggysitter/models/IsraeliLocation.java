package com.example.doggysitter.models;

public class IsraeliLocation {
    private String id;
    private String nameHe;
    private double lat;
    private double lng;

    public IsraeliLocation(String id, String nameHe, double lat, double lng) {
        this.id = id;
        this.nameHe = nameHe;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public String getNameHe() {
        return nameHe;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public String toString() {
        return nameHe;
    }
}
