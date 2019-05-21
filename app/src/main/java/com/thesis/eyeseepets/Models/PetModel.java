package com.thesis.eyeseepets.Models;

import com.google.gson.annotations.SerializedName;

public class PetModel {
    private Integer id;
    private String name;
    private String image;
    private String birthdate;
    private String description;
    private String breed;
    private String gender;
    private Integer owner;
    private Integer fence;
    @SerializedName("assigned_sim")
    private String assignedSim;
    private String color;
    private String size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFence() {
        return fence;
    }

    public void setFence(Integer fence) {
        this.fence = fence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedSim() {
        return assignedSim;
    }

    public void setAssignedSim(String assignedSim) {
        this.assignedSim = assignedSim;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
