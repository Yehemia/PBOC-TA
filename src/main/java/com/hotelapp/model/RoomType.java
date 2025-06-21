package com.hotelapp.model;

import java.util.ArrayList;
import java.util.List;

public class RoomType {
    private int id;
    private String name;
    private double price;
    private String description;
    private int maxGuests;
    private String bedInfo;
    private String imageUrl;
    private int availableRoomCount;
    private List<Facility> facilities;

    public RoomType(int id, String name, double price, String description, int maxGuests, String bedInfo, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.maxGuests = maxGuests;
        this.bedInfo = bedInfo;
        this.imageUrl = imageUrl;
        this.facilities = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public List<Facility> getFacilities() { return facilities; }
    public void setFacilities(List<Facility> facilities) { this.facilities = facilities; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getBedInfo() {
        return bedInfo;
    }

    public void setBedInfo(String bedInfo) {
        this.bedInfo = bedInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getAvailableRoomCount() {
        return availableRoomCount;
    }

    public void setAvailableRoomCount(int availableRoomCount) {
        this.availableRoomCount = availableRoomCount;
    }
}