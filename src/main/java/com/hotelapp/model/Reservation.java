package com.hotelapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private int id;
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

    // Getter dan setter untuk semua properti
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    // Metode tambahan untuk modul penalty

    /**
     * Mengembalikan waktu check-out standar (expected) yaitu tanggal checkOut pada pukul 12:00 (NOON).
     */
    public LocalDateTime getExpectedCheckOutTime() {
        return this.checkOut.atTime(LocalTime.NOON);
    }

    /**
     * Menghitung penalty berdasarkan keterlambatan check-out.
     * Jika checkOutTime (aktual) terlambat dari waktu check-out yang diharapkan,
     * penalty dihitung sebesar Rp50.000 per jam keterlambatan.
     */
    public double calculatePenalty() {
        double penalty = 0;
        if (this.checkOutTime != null) {
            LocalDateTime expectedTime = getExpectedCheckOutTime();
            if (this.checkOutTime.isAfter(expectedTime)) {
                long hoursLate = ChronoUnit.HOURS.between(expectedTime, this.checkOutTime);
                penalty = hoursLate * 50000; // Rp50.000 per jam
            }
        }
        return penalty;
    }
}