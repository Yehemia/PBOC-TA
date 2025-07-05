package com.hotelapp.model;

import java.time.LocalDateTime;

/**
 * Ini adalah "cetakan" untuk data Pembayaran.
 * Kelas ini merepresentasikan sebuah transaksi pembayaran yang terkait dengan reservasi.
 * Catatan: Di aplikasi ini, status pembayaran lebih sering dikelola langsung di objek Reservasi,
 * jadi kelas ini mungkin tidak banyak digunakan secara langsung.
 */
public class Payment {
    // Variabel untuk menyimpan data pembayaran.
    private int id; // ID unik pembayaran di database.
    private int reservationId; // ID reservasi yang dibayar.
    private double amount; // Jumlah uang yang dibayar.
    private String status; // Status pembayaran (misal: 'paid' atau 'unpaid').
    private LocalDateTime paymentDate; // Waktu kapan pembayaran dilakukan.

    /**
     * Constructor untuk membuat objek Pembayaran baru.
     * @param reservationId ID reservasi terkait.
     * @param amount Jumlah pembayaran.
     * @param status Status pembayaran.
     */
    public Payment(int reservationId, double amount, String status) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
    }

    // -- GETTER DAN SETTER --
    // Fungsi untuk mengambil dan mengubah nilai variabel.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}