package com.hotelapp.model;

import java.time.LocalDateTime;

public class Penalty {
    private int id;
    private int reservationId;
    private double amount;
    private String reason;
    private String penaltyStatus;
    private LocalDateTime createdAt;

    public Penalty(int reservationId, double amount, String reason, String penaltyStatus) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.reason = reason;
        this.penaltyStatus = penaltyStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Penalty() { }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getReservationId() {
        return reservationId;
    }
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getPenaltyStatus() {
        return penaltyStatus;
    }
    public void setPenaltyStatus(String penaltyStatus) {
        this.penaltyStatus = penaltyStatus;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}