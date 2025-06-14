package com.hotelapp.model;

import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int reservationId;
    private double amount;
    private String status; // 'paid' atau 'unpaid'
    private LocalDateTime paymentDate;

    public Payment(int reservationId, double amount, String status) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
    }

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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

}
