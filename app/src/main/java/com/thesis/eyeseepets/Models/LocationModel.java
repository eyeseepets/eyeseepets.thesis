package com.thesis.eyeseepets.Models;

import com.google.gson.annotations.SerializedName;

public class LocationModel {
    private String date;
    private Integer pet;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    @SerializedName("is_within_bounds")
    private Boolean withinBounds;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getPet() {
        return pet;
    }

    public void setPet(Integer pet) {
        this.pet = pet;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Boolean getWithinBounds() {
        return withinBounds;
    }

    public void setWithinBounds(Boolean withinBounds) {
        this.withinBounds = withinBounds;
    }
}
