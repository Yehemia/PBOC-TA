package com.hotelapp.model;

public class Room {
    private int id;
    private int roomNumber;
    private String status;
    private RoomType roomType;

    public Room(int id, int roomNumber, String status, RoomType roomType) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.status = status;
        this.roomType = roomType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
}