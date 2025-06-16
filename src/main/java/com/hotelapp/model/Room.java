package com.hotelapp.model;

public class Room {
    private int id;
    private int roomNumber;
    private String roomType;
    private double price;
    private String imageUrl;
    private String status;

    // Constructor lengkap
    public Room(int id, int roomNumber, String roomType, double price, String imageUrl, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    // Constructor untuk membuat kamar baru (ID belum ada)
    public Room(int roomNumber, String roomType, double price, String imageUrl) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(String status) { this.status = status; }
}