package com.hotelapp.model;

/**
 * Ini adalah "cetakan" untuk data Kamar Fisik (Unit Kamar).
 * Merepresentasikan satu kamar nyata di hotel dengan nomor tertentu.
 */
public class Room {
    // Variabel untuk menyimpan data kamar.
    private int id; // ID unik kamar di database.
    private int roomNumber; // Nomor kamar yang terlihat oleh tamu (misal: 101, 205).
    private String status; // Status kamar saat ini ('available', 'booked', 'maintenance').

    // Sebuah kamar memiliki satu tipe kamar. Ini adalah hubungan antar objek.
    private RoomType roomType; // Objek RoomType yang berisi detail (harga, nama, dll).

    /**
     * Constructor untuk membuat objek Room baru.
     * @param id ID kamar.
     * @param roomNumber Nomor kamar.
     * @param status Status kamar.
     * @param roomType Objek RoomType terkait.
     */
    public Room(int id, int roomNumber, String status, RoomType roomType) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.status = status;
        this.roomType = roomType;
    }

    // -- GETTER DAN SETTER --
    // Fungsi untuk mengambil dan mengubah nilai setiap variabel.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
}