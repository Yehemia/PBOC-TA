package com.hotelapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private int id;
    private String bookingCode;
    private int userId;
    private int roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String paymentMethod;
    private String bookingType;
    private String status;
    private double totalPrice;
    private String penaltyStatus;
    private String paymentStatus;
    private String guestName;


    private String roomTypeName;
    private int roomNumber;


    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut, LocalDateTime checkInTime, LocalDateTime checkOutTime, String paymentMethod, String bookingType, String status, double totalPrice, String penaltyStatus, String paymentStatus, String guestName) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.penaltyStatus = penaltyStatus;
        this.paymentStatus = paymentStatus;
        this.guestName = guestName;
    }

    public Reservation(int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String bookingType, String status, double totalPrice, String guestName) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.guestName = guestName;
    }

    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String bookingType, String status, double totalPrice, String guestName) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.guestName = guestName;
    }

    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String status, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public Reservation(int id, LocalDate checkIn, LocalDate checkOut, String status, String roomTypeName, int roomNumber) {
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.roomTypeName = roomTypeName;
        this.roomNumber = roomNumber;
    }

    public Reservation(int id, int roomId, LocalDate checkIn, LocalDate checkOut, String status, String roomTypeName, int roomNumber) {
        this.id = id;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.roomTypeName = roomTypeName;
        this.roomNumber = roomNumber;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getPenaltyStatus() {
        return penaltyStatus;
    }

    public void setPenaltyStatus(String penaltyStatus) {
        this.penaltyStatus = penaltyStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public LocalDateTime getExpectedCheckOutTime() {
        return this.checkOut.atTime(LocalTime.NOON);
    }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public double calculatePenalty() {
        double penalty = 0;
        if (this.checkOutTime != null) {
            LocalDateTime expectedTime = getExpectedCheckOutTime();
            if (this.checkOutTime.isAfter(expectedTime)) {
                long hoursLate = ChronoUnit.HOURS.between(expectedTime, this.checkOutTime);
                penalty = hoursLate * 50000;
            }
        }
        return penalty;
    }
}
