package com.hotelapp.model;

import javafx.beans.property.*;

public class Room {
    private final IntegerProperty id;
    private final IntegerProperty roomNumber;
    private final StringProperty roomType;
    private final DoubleProperty price;
    private final StringProperty imageUrl; // URL atau path ke gambar kamar

    public Room(Integer id, Integer roomNumber, String roomType, double price, String imageUrl) {
        this.id = new SimpleIntegerProperty(id);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.roomType = new SimpleStringProperty(roomType);
        this.price = new SimpleDoubleProperty(price);
        this.imageUrl = new SimpleStringProperty(imageUrl);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getter dan Setter untuk roomNumber
    public Integer getRoomNumber() {
        return roomNumber.get();
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber.set(roomNumber);
    }

    public IntegerProperty roomNumberProperty() {
        return roomNumber;
    }

    // Getter dan Setter untuk roomType
    public String getRoomType() {
        return roomType.get();
    }

    public void setRoomType(String roomType) {
        this.roomType.set(roomType);
    }

    public StringProperty roomTypeProperty() {
        return roomType;
    }

    // Getter dan Setter untuk price
    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    // Getter dan Setter untuk imageUrl
    public String getImageUrl() {
        return imageUrl.get();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }
}

