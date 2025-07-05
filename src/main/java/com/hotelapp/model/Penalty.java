package com.hotelapp.model;

import java.time.LocalDateTime;

/**
 * Ini adalah "cetakan" untuk data Denda.
 * Merepresentasikan denda yang mungkin timbul dari sebuah reservasi (misal: kerusakan properti).
 */
public class Penalty {
    // Variabel untuk menyimpan data denda.
    private int id; // ID unik denda.
    private int reservationId; // ID reservasi yang terkena denda.
    private double amount; // Jumlah denda.
    private String reason; // Alasan mengapa denda diberikan.
    private String penaltyStatus; // Status denda (misal: 'pending' atau 'paid').
    private LocalDateTime createdAt; // Waktu kapan denda dibuat.

    /**
     * Constructor untuk membuat objek Denda baru dengan data lengkap.
     * @param reservationId ID reservasi terkait.
     * @param amount Jumlah denda.
     * @param reason Alasan denda.
     * @param penaltyStatus Status denda.
     */
    public Penalty(int reservationId, double amount, String reason, String penaltyStatus) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.reason = reason;
        this.penaltyStatus = penaltyStatus;
        this.createdAt = LocalDateTime.now(); // Waktu dibuat di-set secara otomatis saat objek dibuat.
    }

    /**
     * Constructor kosong. Berguna saat kita ingin membuat objek kosong terlebih dahulu,
     * lalu mengisi datanya satu per satu menggunakan setter.
     */
    public Penalty() { }

    // -- GETTER DAN SETTER --
    // Fungsi untuk mengambil dan mengubah nilai setiap variabel.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPenaltyStatus() { return penaltyStatus; }
    public void setPenaltyStatus(String penaltyStatus) { this.penaltyStatus = penaltyStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}